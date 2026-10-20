package com.look4counter.jsp

import com.intellij.application.options.CodeStyle
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.highlighter.JavaFileHighlighter
import com.intellij.injected.editor.DocumentWindow
import com.intellij.lang.ASTNode
import com.intellij.lang.html.HTMLLanguage
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.Key
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.codeStyle.PostFormatProcessor
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor

/** Projects Java formatting back before the HTML formatter adjusts surrounding text. */
class JspJavaPreFormatProcessor : PreFormatProcessor {
    override fun process(element: ASTNode, range: TextRange): TextRange =
        JspJavaFormatting.process(element.psi.containingFile, range, false,
            CodeStyle.getSettings(element.psi.containingFile))

    override fun changesWhitespacesOnly() = true
}

/** Declaration margins depend on the final position assigned by the HTML formatter. */
class JspDeclarationPostFormatProcessor : PostFormatProcessor {
    override fun processText(file: PsiFile, range: TextRange, settings: CodeStyleSettings): TextRange =
        JspJavaFormatting.process(file, range, true, settings)

    override fun processElement(source: PsiElement, settings: CodeStyleSettings): PsiElement {
        if (source.containingFile !is JspPsiFile) return source
        val pointer = SmartPointerManager.createPointer(source)
        val file = source.containingFile
        processText(file, source.textRange, settings)
        return pointer.element ?: file
    }

    override fun isWhitespaceOnly() = true
}

private object JspJavaFormatting {
    private val declarationRanges = Key.create<List<RangeMarker>>("jsp.declaration.format.ranges")

    fun process(file: PsiFile, range: TextRange, declarations: Boolean, settings: CodeStyleSettings): TextRange {
        if (file !is JspPsiFile) return range
        val manager = PsiDocumentManager.getInstance(file.project)
        val document = manager.getDocument(file) ?: return range
        if (declarations) manager.commitDocument(document)
        val injectionManager = InjectedLanguageManager.getInstance(file.project)
        if (!declarations) {
            val previous = document.getUserData(declarationRanges).orEmpty().filter { marker ->
                val keep = marker.isValid && !range.intersects(TextRange(marker.startOffset, marker.endOffset))
                if (!keep) marker.dispose()
                keep
            }
            val selectedDeclarations = file.children.filterIsInstance<JspJavaInjectionHost>().filter { host ->
                range.contains(host.textRange) && isMultilineDeclaration(host, document.charsSequence)
            }.map { host ->
                val lineStart = document.getLineStartOffset(document.getLineNumber(host.textRange.startOffset))
                val start = if (range.startOffset <= lineStart) lineStart else host.textRange.startOffset
                document.createRangeMarker(start, host.textRange.endOffset)
            }
            document.putUserData(declarationRanges, previous + selectedDeclarations)
        }
        val pending = document.getUserData(declarationRanges).orEmpty()
        val selected = file.children.filterIsInstance<JspJavaInjectionHost>().mapNotNull { host ->
            if (declarations && !range.contains(host.textRange)) return@mapNotNull null
            val declaration = pending.any { it.isValid && it.startOffset <= host.textRange.startOffset &&
                it.endOffset == host.textRange.endOffset }
            if (declaration != declarations) return@mapNotNull null
            val body = host.bodyRange.shiftRight(host.textRange.startOffset)
            body.intersection(range)?.takeUnless { it.isEmpty }?.let { host to it }
        }
        val replacements = mutableListOf<Replacement>()
        val html = if (declarations) file.viewProvider.getPsi(HTMLLanguage.INSTANCE) else null
        val noHtmlTags = html != null && PsiTreeUtil.findChildOfType(html, XmlTag::class.java) == null
        val layouts = if (declarations) selected.associate { (host, body) ->
            val lineStart = document.getLineStartOffset(document.getLineNumber(host.textRange.startOffset))
            var outer = document.charsSequence.subSequence(lineStart, host.textRange.startOffset).toString()
            val canAlignOpening = pending.any { it.isValid && it.startOffset <= lineStart &&
                it.endOffset == host.textRange.endOffset }
            if (noHtmlTags && canAlignOpening && outer.isNotEmpty()) {
                // A JSP-only file may have no HTML blocks to normalize this whitespace.
                replacements += Replacement(TextRange(lineStart, host.textRange.startOffset), "")
                outer = ""
            }
            val options = settings.getIndentOptions(JavaFileType.INSTANCE)
            val size = options.INDENT_SIZE.coerceAtLeast(0)
            val step = if (options.USE_TAB_CHARACTER && options.TAB_SIZE > 0)
                "\t".repeat(size / options.TAB_SIZE) + " ".repeat(size % options.TAB_SIZE) else " ".repeat(size)
            body to DeclarationLayout(outer, outer + step)
        } else emptyMap()
        val groups = selected.mapNotNull { (host, body) ->
            val java = injectionManager.getInjectedPsiFiles(host)?.firstOrNull()?.first as? PsiFile
            java?.let { it to body }
        }.groupBy({ it.first }, { it.second })
        for ((java, bodies) in groups) {
            val window = manager.getDocument(java) as? DocumentWindow ?: continue
            val copy = PsiFileFactory.getInstance(file.project)
                .createFileFromText("__JspPage.java", JavaFileType.INSTANCE, java.text, 0, true)
            val copyDocument = copy.viewProvider.document ?: continue
            val fragments = bodies.mapNotNull { body ->
                // Delimiter-adjacent whitespace belongs to JSP, not the virtual class.
                var start = body.startOffset
                var end = body.endOffset
                while (start < end && document.charsSequence[start].isWhitespace()) start++
                while (end > start && document.charsSequence[end - 1].isWhitespace()) end--
                if (start == end) null else Fragment(TextRange(start, end), body,
                    copyDocument.createRangeMarker(window.hostToInjected(start), window.hostToInjected(end)))
            }
            if (fragments.isEmpty()) continue
            try {
                CodeStyleManager.getInstance(file.project).reformatText(copy,
                    fragments.map { TextRange(it.marker.startOffset, it.marker.endOffset) }.sortedBy { it.startOffset })
                for (fragment in fragments) {
                    if (!fragment.marker.isValid) continue
                    val start = fragment.marker.startOffset
                    val lineStart = copyDocument.getLineStartOffset(copyDocument.getLineNumber(start))
                    val baseIndent = copyDocument.charsSequence.subSequence(lineStart, start)
                        .takeWhile { it == ' ' || it == '\t' }.length
                    val formatted = preserveTokens(document.getText(fragment.hostRange),
                        copyDocument.getText(TextRange(start, fragment.marker.endOffset))) ?: continue
                    var replacement = removeVirtualIndent(formatted, baseIndent)
                    val layout = layouts[fragment.bodyRange]
                    val target = if (layout == null) fragment.hostRange else fragment.bodyRange
                    if (layout != null) replacement = "\n" + layout.code +
                        addIndent(replacement, layout.code) + "\n" + layout.outer
                    val original = document.getText(target)
                    if (original.filterNot(Char::isWhitespace) == replacement.filterNot(Char::isWhitespace)) {
                        replacements += Replacement(target, replacement)
                    }
                }
            } finally {
                fragments.forEach { it.marker.dispose() }
            }
        }
        // An empty declaration has no injected Java fragment to format.
        for ((body, layout) in layouts) {
            if (document.getText(body).isBlank()) replacements += Replacement(body, "\n" + layout.outer)
        }
        val oldLength = document.textLength
        val updatedRange = document.createRangeMarker(range)
        for ((hostRange, text) in replacements.sortedByDescending { it.range.startOffset }) {
            document.replaceString(hostRange.startOffset, hostRange.endOffset, text)
        }
        manager.commitDocument(document)
        if (declarations) {
            val remaining = pending.filter { marker ->
                val keep = marker.isValid && !range.intersects(TextRange(marker.startOffset, marker.endOffset))
                if (!keep) marker.dispose()
                keep
            }
            document.putUserData(declarationRanges, remaining.takeUnless { it.isEmpty() })
        }
        val result = if (declarations && updatedRange.isValid)
            TextRange(updatedRange.startOffset, updatedRange.endOffset)
        else TextRange(range.startOffset, range.endOffset + document.textLength - oldLength)
        updatedRange.dispose()
        return result
    }

    private fun preserveTokens(original: String, formatted: String): String? {
        val originalLexer = JavaFileHighlighter().highlightingLexer
        val formattedLexer = JavaFileHighlighter().highlightingLexer
        originalLexer.start(original)
        formattedLexer.start(formatted)
        val result = StringBuilder()
        while (formattedLexer.tokenType != null) {
            val token = formatted.substring(formattedLexer.tokenStart, formattedLexer.tokenEnd)
            if (formattedLexer.tokenType == TokenType.WHITE_SPACE) result.append(token)
            else {
                while (originalLexer.tokenType == TokenType.WHITE_SPACE) originalLexer.advance()
                if (originalLexer.tokenType != formattedLexer.tokenType) return null
                val source = original.substring(originalLexer.tokenStart, originalLexer.tokenEnd)
                if (source.filterNot(Char::isWhitespace) != token.filterNot(Char::isWhitespace)) return null
                result.append(source)
                originalLexer.advance()
            }
            formattedLexer.advance()
        }
        while (originalLexer.tokenType == TokenType.WHITE_SPACE) originalLexer.advance()
        return result.toString().takeIf { originalLexer.tokenType == null }
    }

    private fun isMultilineDeclaration(host: JspJavaInjectionHost, text: CharSequence): Boolean {
        if (host.kind != JspTokenTypes.DECLARATION || !host.text.endsWith("%>")) return false
        val before = text.subSequence(0, host.textRange.startOffset).toString().substringAfterLast('\n')
        if (before.any { it != ' ' && it != '\t' }) return false
        return '\n' in host.bodyRange.substring(host.text).takeWhile(Char::isWhitespace)
    }

    private fun addIndent(text: String, indent: String): String {
        val lexer = JavaFileHighlighter().highlightingLexer
        lexer.start(text)
        return buildString {
            while (lexer.tokenType != null) {
                val token = text.substring(lexer.tokenStart, lexer.tokenEnd)
                append(if (lexer.tokenType == TokenType.WHITE_SPACE) token.replace("\n", "\n$indent") else token)
                lexer.advance()
            }
        }
    }

    private fun removeVirtualIndent(text: String, indent: Int): String {
        if (indent == 0 || '\n' !in text) return text
        val lexer = JavaFileHighlighter().highlightingLexer
        lexer.start(text)
        return buildString {
            while (lexer.tokenType != null) {
                val token = text.substring(lexer.tokenStart, lexer.tokenEnd)
                if (lexer.tokenType == TokenType.WHITE_SPACE) {
                    append(token.replace(Regex("\n[ \\t]{0,$indent}"), "\n"))
                } else append(token)
                lexer.advance()
            }
        }
    }

    private data class Fragment(val hostRange: TextRange, val bodyRange: TextRange, val marker: RangeMarker)
    private data class DeclarationLayout(val outer: String, val code: String)
    private data class Replacement(val range: TextRange, val text: String)
}
