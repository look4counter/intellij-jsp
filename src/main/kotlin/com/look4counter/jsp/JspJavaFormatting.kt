package com.look4counter.jsp

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.ide.highlighter.JavaFileHighlighter
import com.intellij.injected.editor.DocumentWindow
import com.intellij.lang.ASTNode
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.TokenType
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.impl.source.codeStyle.PreFormatProcessor

/** Projects Java formatting back before the HTML formatter adjusts surrounding text. */
class JspJavaPreFormatProcessor : PreFormatProcessor {
    override fun process(element: ASTNode, range: TextRange): TextRange {
        val file = element.psi.containingFile
        if (file !is JspPsiFile) return range
        val manager = PsiDocumentManager.getInstance(file.project)
        val document = manager.getDocument(file) ?: return range
        val injectionManager = InjectedLanguageManager.getInstance(file.project)
        val selected = file.children.filterIsInstance<JspJavaInjectionHost>().mapNotNull { host ->
            val body = host.bodyRange.shiftRight(host.textRange.startOffset)
            body.intersection(range)?.takeUnless { it.isEmpty }?.let { host to it }
        }
        val groups = selected.mapNotNull { (host, body) ->
            val java = injectionManager.getInjectedPsiFiles(host)?.firstOrNull()?.first as? PsiFile
            java?.let { it to body }
        }.groupBy({ it.first }, { it.second })
        val replacements = mutableListOf<Replacement>()
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
                if (start == end) null else Fragment(TextRange(start, end),
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
                    val replacement = removeVirtualIndent(
                        copyDocument.getText(TextRange(start, fragment.marker.endOffset)), baseIndent)
                    val original = document.getText(fragment.hostRange)
                    if (original.filterNot(Char::isWhitespace) == replacement.filterNot(Char::isWhitespace)) {
                        replacements += Replacement(fragment.hostRange, replacement)
                    }
                }
            } finally {
                fragments.forEach { it.marker.dispose() }
            }
        }
        val oldLength = document.textLength
        for ((hostRange, text) in replacements.sortedByDescending { it.range.startOffset }) {
            document.replaceString(hostRange.startOffset, hostRange.endOffset, text)
        }
        manager.commitDocument(document)
        return TextRange(range.startOffset, range.endOffset + document.textLength - oldLength)
    }

    override fun changesWhitespacesOnly() = true

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

    private data class Fragment(val hostRange: TextRange, val marker: RangeMarker)
    private data class Replacement(val range: TextRange, val text: String)
}
