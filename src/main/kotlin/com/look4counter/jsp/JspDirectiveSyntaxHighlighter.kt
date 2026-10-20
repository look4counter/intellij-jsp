package com.look4counter.jsp

import com.intellij.lexer.LexerBase
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType

class JspDirectiveSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getTokenHighlights(tokenType: IElementType) = when (tokenType) {
        DELIMITER -> pack(JspSyntaxHighlighter.DIRECTIVE_DELIMITER)
        NAME -> pack(JspSyntaxHighlighter.DIRECTIVE_NAME)
        ATTRIBUTE -> pack(JspSyntaxHighlighter.DIRECTIVE_ATTRIBUTE)
        EQUALS -> pack(JspSyntaxHighlighter.DIRECTIVE_EQUALS)
        STRING -> pack(JspSyntaxHighlighter.DIRECTIVE_STRING)
        else -> emptyArray()
    }

    override fun getHighlightingLexer() = object : LexerBase() {
        private var buffer: CharSequence = ""
        private var end = 0
        private var index = 0
        private val tokens = mutableListOf<Token>()

        override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
            this.buffer = buffer
            end = endOffset
            index = 0
            tokens.clear()
            fun add(type: IElementType, begin: Int, limit: Int) {
                if (limit > begin && limit > startOffset) tokens += Token(type, begin.coerceAtLeast(startOffset), limit)
            }
            val outer = JspHighlightingLexer()
            outer.start(buffer, 0, endOffset, 0)
            while (outer.tokenType != null) {
                val begin = outer.tokenStart
                val limit = outer.tokenEnd
                if (outer.tokenType != JspTokenTypes.DIRECTIVE) {
                    add(TokenType.WHITE_SPACE, begin, limit)
                } else {
                    val bodyStart = (begin + 3).coerceAtMost(limit)
                    val bodyEnd = if (buffer.subSequence(begin, limit).endsWith("%>")) limit - 2 else limit
                    add(DELIMITER, begin, bodyStart)
                    var position = bodyStart
                    var firstName = true
                    var value = false
                    while (position < bodyEnd) {
                        val tokenStart = position
                        val char = buffer[position]
                        val type = when {
                            char.isWhitespace() -> {
                                while (position < bodyEnd && buffer[position].isWhitespace()) position++
                                TokenType.WHITE_SPACE
                            }
                            char == '=' -> { position++; value = true; EQUALS }
                            char == '\'' || char == '"' -> {
                                position++
                                while (position < bodyEnd && buffer[position] != char) position++
                                if (position < bodyEnd) position++
                                value = false
                                STRING
                            }
                            else -> {
                                while (position < bodyEnd && !buffer[position].isWhitespace() && buffer[position] !in "='\"") position++
                                when {
                                    firstName -> { firstName = false; NAME }
                                    value -> { value = false; STRING }
                                    else -> ATTRIBUTE
                                }
                            }
                        }
                        add(type, tokenStart, position)
                    }
                    add(DELIMITER, bodyEnd, limit)
                }
                outer.advance()
            }
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
    companion object {
        val DELIMITER = IElementType("JSP_DIRECTIVE_DELIMITER", JspLanguage)
        val NAME = IElementType("JSP_DIRECTIVE_NAME", JspLanguage)
        val ATTRIBUTE = IElementType("JSP_DIRECTIVE_ATTRIBUTE", JspLanguage)
        val EQUALS = IElementType("JSP_DIRECTIVE_EQUALS", JspLanguage)
        val STRING = IElementType("JSP_DIRECTIVE_STRING", JspLanguage)
    }
}
