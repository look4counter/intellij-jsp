package com.look4counter.jsp

import com.intellij.ide.highlighter.JavaFileHighlighter
import com.intellij.lexer.LexerBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

/** Keeps JSP delimiters outside the Java lexer, including during PSI rebuilds. */
class JspJavaSyntaxHighlighter(private val hostType: IElementType) : SyntaxHighlighterBase() {
    private val java = JavaFileHighlighter()
    override fun getTokenHighlights(tokenType: IElementType) =
        if (tokenType == hostType) JspSyntaxHighlighter().getTokenHighlights(hostType) else java.getTokenHighlights(tokenType)

    override fun getHighlightingLexer() = object : LexerBase() {
        private var buffer: CharSequence = ""
        private var end = 0
        private var index = 0
        private val tokens = mutableListOf<Token>()

        override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
            this.buffer = buffer
            end = endOffset
            tokens.clear()
            index = 0
            // Layers concatenate equal host types. Scan all fragments so comments and
            // unfinished strings in one fragment never consume the next JSP delimiter.
            val outer = JspHighlightingLexer()
            outer.start(buffer, 0, endOffset, 0)
            while (outer.tokenType != null) {
                val begin = outer.tokenStart
                val limit = outer.tokenEnd
                if (outer.tokenType == hostType) {
                    val bodyStart = (begin + if (hostType == JspTokenTypes.SCRIPTLET) 2 else 3).coerceAtMost(limit)
                    val bodyEnd = if (limit - begin >= 2 && buffer.subSequence(limit - 2, limit).toString() == "%>")
                        (limit - 2).coerceAtLeast(bodyStart) else limit
                    add(hostType, begin, bodyStart, startOffset)
                    val delegate = java.highlightingLexer
                    delegate.start(buffer, bodyStart, bodyEnd)
                    while (delegate.tokenType != null) {
                        add(delegate.tokenType!!, delegate.tokenStart, delegate.tokenEnd, startOffset)
                        delegate.advance()
                    }
                    add(hostType, bodyEnd, limit, startOffset)
                } else add(TokenType.WHITE_SPACE, begin, limit, startOffset)
                outer.advance()
            }
        }

        private fun add(type: IElementType, begin: Int, limit: Int, startOffset: Int) {
            if (limit > begin && limit > startOffset) tokens += Token(type, begin.coerceAtLeast(startOffset), limit)
        }

        override fun advance() { index++ }
        override fun getState() = 0
        override fun getTokenType() = tokens.getOrNull(index)?.type
        override fun getTokenStart() = tokens.getOrNull(index)?.start ?: end
        override fun getTokenEnd() = tokens.getOrNull(index)?.end ?: end
        override fun getBufferSequence() = buffer
        override fun getBufferEnd() = end
    }

    private data class Token(val type: IElementType, val start: Int, val end: Int)
}
