package com.look4counter.jsp

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.progress.ProgressManager
import com.intellij.codeInsight.AutoPopupController
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiFile
import com.intellij.psi.search.PsiShortNamesCache

class JspDirectiveCompletionContributor : CompletionContributor(), DumbAware {
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        if (parameters.completionType != CompletionType.BASIC) return
        val file = parameters.originalFile.viewProvider.getPsi(JspLanguage) ?: return
        val text = file.text
        val offset = parameters.offset
        val lexer = JspHighlightingLexer()
        lexer.start(text)
        while (lexer.tokenType != null && lexer.tokenEnd < offset) lexer.advance()
        if (lexer.tokenType != JspTokenTypes.DIRECTIVE || offset < lexer.tokenStart + 3 || offset > lexer.tokenEnd) return
        val token = text.substring(lexer.tokenStart, lexer.tokenEnd)
        val before = text.substring(lexer.tokenStart + 3, offset)
        val context = context(before) ?: return
        if (context.kind == Kind.VALUE && context.directive == "page" && context.attribute == "import") {
            importValues(file, offset, context, result)
            result.stopHere()
            return
        }
        if (context.kind == Kind.VALUE && context.directive == "include" && context.attribute == "file") {
            includeValues(file, offset, context, result)
            result.stopHere()
            return
        }
        val matching = result.withPrefixMatcher(context.prefix)
        when (context.kind) {
            Kind.DIRECTIVE -> for (name in ATTRIBUTES.keys) {
                matching.addElement(LookupElementBuilder.create(name).withInsertHandler { insertion, _ -> insertNameSpace(insertion) })
            }
            Kind.ATTRIBUTE -> {
                val present = JspDirectiveSyntax.parse(token)?.attributes.orEmpty()
                for (name in ATTRIBUTES[context.directive].orEmpty()) {
                    if (name in present && !(name == "import" && context.directive == "page")) continue
                    if (context.directive == "taglib" && ((name == "uri" && "tagdir" in present) || (name == "tagdir" && "uri" in present))) continue
                    matching.addElement(LookupElementBuilder.create(name).withInsertHandler { insertion, _ -> insertAttribute(insertion) })
                }
            }
            Kind.VALUE -> {
                val suffix = suffix(text, offset) { it != context.quote && (context.quote != null || !it.isWhitespace()) && it !in "<>" }
                for (value in values(context.directive, context.attribute)) {
                    matching.addElement(LookupElementBuilder.create(value).withInsertHandler { insertion, _ ->
                        insertDynamicValue(insertion, context, offset - context.prefix.length, suffix, value)
                    })
                }
            }
        }
        result.stopHere()
    }

    private fun insertNameSpace(context: InsertionContext) {
        val offset = context.tailOffset
        if (offset == context.document.textLength || !context.document.charsSequence[offset].isWhitespace()) {
            context.document.insertString(offset, " ")
        }
        context.editor.caretModel.moveToOffset(offset + 1)
        if (context.completionChar == ' ') context.setAddCompletionChar(false)
    }

    private fun insertAttribute(context: InsertionContext) {
        val document = context.document
        var offset = context.tailOffset
        while (offset < document.textLength && document.charsSequence[offset].isWhitespace()) offset++
        if (offset >= document.textLength || document.charsSequence[offset] != '=') {
            document.insertString(context.tailOffset, "=\"\"")
            context.editor.caretModel.moveToOffset(context.tailOffset - 1)
        } else {
            offset++
            while (offset < document.textLength && document.charsSequence[offset].isWhitespace()) offset++
            if (offset < document.textLength && document.charsSequence[offset] in "\"'") {
                context.editor.caretModel.moveToOffset(offset + 1)
            } else {
                document.insertString(offset, "\"\"")
                context.editor.caretModel.moveToOffset(offset + 1)
            }
        }
        if (context.completionChar == '=' || context.completionChar == ' ') context.setAddCompletionChar(false)
        AutoPopupController.getInstance(context.project).scheduleAutoPopup(context.editor)
    }

    private fun importValues(file: PsiFile, offset: Int, context: Context, result: CompletionResultSet) {
        if (DumbService.isDumb(file.project)) return
        val prefix = context.prefix.substringAfterLast(',').trimStart()
        val matching = result.withPrefixMatcher(prefix)
        val start = offset - prefix.length
        val suffix = suffix(file.text, offset) { Character.isJavaIdentifierPart(it) || it == '.' }
        val facade = JavaPsiFacade.getInstance(file.project)
        val scope = file.resolveScope
        val packageName = prefix.substringBeforeLast('.', "")
        val emitted = mutableSetOf<String>()
        fun addClass(target: PsiClass) {
            ProgressManager.checkCanceled()
            val name = target.qualifiedName ?: return
            if (!emitted.add(name) || !facade.resolveHelper.isAccessible(target, file, null)) return
            matching.addElement(LookupElementBuilder.create(target, name).withLookupString(target.name.orEmpty())
                .withPresentableText(target.name.orEmpty()).withTypeText(name.substringBeforeLast('.'))
                .withInsertHandler { insertion, _ -> insertDynamicValue(insertion, context, start, suffix, name) })
        }
        val pkg = facade.findPackage(packageName)
        for (child in pkg?.getSubPackages(scope).orEmpty()) {
            ProgressManager.checkCanceled()
            val name = child.qualifiedName
            matching.addElement(LookupElementBuilder.create(name).withTypeText("패키지")
                .withInsertHandler { insertion, _ -> insertDynamicValue(insertion, context, start, suffix, "$name.", true) })
        }
        if ('.' in prefix) {
            pkg?.getClasses(scope)?.forEach(::addClass)
            facade.findClass(packageName, scope)?.innerClasses?.forEach(::addClass)
        } else if (prefix.isNotEmpty()) {
            val cache = PsiShortNamesCache.getInstance(file.project)
            val names = mutableListOf<String>()
            cache.processAllClassNames({ name ->
                ProgressManager.checkCanceled()
                if (matching.prefixMatcher.prefixMatches(name)) names += name
                true
            }, scope, null)
            for (name in names) {
                ProgressManager.checkCanceled()
                cache.getClassesByName(name, scope).forEach(::addClass)
            }
        }
    }

    private fun includeValues(file: PsiFile, offset: Int, context: Context, result: CompletionResultSet) {
        val source = file.originalFile.virtualFile ?: return
        val directoryPath = context.prefix.substringBeforeLast('/', "").let {
            if ('/' in context.prefix) "$it/" else ""
        }
        val directory = JspIncludeContext.resolveInclude(source, directoryPath, file.project, directory = true) ?: return
        val prefix = context.prefix.substringAfterLast('/')
        val matching = result.withPrefixMatcher(prefix)
        val start = offset - prefix.length
        val suffix = suffix(file.text, offset) { it != '/' && it != context.quote && (context.quote != null || !it.isWhitespace()) && it !in "<>" }
        for (child in directory.children.orEmpty()) {
            ProgressManager.checkCanceled()
            if (child.name.contains(context.quote ?: '"')) continue
            val name = child.name + if (child.isDirectory) "/" else ""
            matching.addElement(LookupElementBuilder.create(name).withTypeText(if (child.isDirectory) "폴더" else "파일")
                .withInsertHandler { insertion, _ -> insertDynamicValue(insertion, context, start, suffix, name, child.isDirectory) })
        }
    }

    private fun suffix(text: String, offset: Int, accepts: (Char) -> Boolean): String {
        var end = offset
        while (end < text.length && accepts(text[end])) end++
        return text.substring(offset, end)
    }

    private fun insertDynamicValue(
        insertion: InsertionContext, context: Context, start: Int, suffix: String, value: String, next: Boolean = false,
    ) {
        val document = insertion.document
        var end = insertion.tailOffset
        if (document.charsSequence.subSequence(end, (end + suffix.length).coerceAtMost(document.textLength)).toString() == suffix) end += suffix.length
        if (value.endsWith('/') && end < document.textLength && document.charsSequence[end] == '/') end++
        document.replaceString(start, end, value)
        var tail = start + value.length
        if (context.quote == null) {
            document.insertString(tail, "\"")
            document.insertString(start, "\"")
            tail++
        }
        insertion.tailOffset = tail
        insertion.editor.caretModel.moveToOffset(tail)
        if (next && insertion.completionChar == value.last()) insertion.setAddCompletionChar(false)
        if (next) AutoPopupController.getInstance(insertion.project).scheduleAutoPopup(insertion.editor)
    }

    private enum class Kind { DIRECTIVE, ATTRIBUTE, VALUE }
    private data class Context(val kind: Kind, val prefix: String, val directive: String = "", val attribute: String = "", val quote: Char? = null)

    private fun context(text: String): Context? {
        var position = 0
        while (position < text.length && text[position].isWhitespace()) position++
        val nameStart = position
        while (position < text.length && text[position].isLetter()) position++
        val directive = text.substring(nameStart, position)
        if (position == text.length) return Context(Kind.DIRECTIVE, directive)
        if (directive !in ATTRIBUTES || !text[position].isWhitespace()) return null
        while (position < text.length) {
            while (position < text.length && text[position].isWhitespace()) position++
            val attributeStart = position
            while (position < text.length && (text[position].isLetterOrDigit() || text[position] in "_:-")) position++
            val attribute = text.substring(attributeStart, position)
            if (position == text.length) return Context(Kind.ATTRIBUTE, attribute, directive)
            while (position < text.length && text[position].isWhitespace()) position++
            if (position == text.length) return Context(Kind.ATTRIBUTE, "", directive)
            if (text[position] != '=') return null
            position++
            while (position < text.length && text[position].isWhitespace()) position++
            if (position == text.length) return Context(Kind.VALUE, "", directive, attribute)
            val quote = text[position].takeIf { it in "\"'" }
            if (quote != null) {
                position++
                val valueStart = position
                while (position < text.length && text[position] != quote) position++
                if (position == text.length) return Context(Kind.VALUE, text.substring(valueStart), directive, attribute, quote)
                position++
            } else {
                val valueStart = position
                while (position < text.length && !text[position].isWhitespace()) position++
                if (position == text.length) return Context(Kind.VALUE, text.substring(valueStart), directive, attribute)
            }
        }
        return Context(Kind.ATTRIBUTE, "", directive)
    }

    private fun values(directive: String, attribute: String): List<String> {
        if (directive != "page") return emptyList()
        return when (attribute) {
            "session", "autoFlush", "isThreadSafe", "isErrorPage", "isELIgnored", "deferredSyntaxAllowedAsLiteral", "trimDirectiveWhitespaces", "errorOnELNotFound" -> listOf("true", "false")
            "language" -> listOf("java")
            "buffer" -> listOf("none", "8kb", "16kb", "32kb", "64kb")
            "pageEncoding" -> listOf("UTF-8", "UTF-16", "ISO-8859-1", "EUC-KR")
            "contentType" -> listOf("text/html; charset=UTF-8", "text/plain; charset=UTF-8", "application/json; charset=UTF-8", "application/xml; charset=UTF-8")
            else -> emptyList()
        }
    }

    companion object {
        private val ATTRIBUTES = linkedMapOf(
            "page" to listOf("language", "extends", "import", "session", "buffer", "autoFlush", "isThreadSafe", "info", "errorPage", "isErrorPage", "contentType", "pageEncoding", "isELIgnored", "deferredSyntaxAllowedAsLiteral", "trimDirectiveWhitespaces", "errorOnELNotFound"),
            "include" to listOf("file"),
            "taglib" to listOf("prefix", "uri", "tagdir"),
        )
    }
}
