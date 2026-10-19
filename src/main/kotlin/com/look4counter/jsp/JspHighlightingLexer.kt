package com.look4counter.jsp

import com.intellij.lexer.LexerBase
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.OuterLanguageElementType

object JspTokenTypes {
    val HTML_TEXT: IElementType = OuterLanguageElementType("JSP_HTML_TEXT", JspLanguage)
    val DIRECTIVE = IElementType("JSP_DIRECTIVE", JspLanguage)
    val SCRIPTLET = IElementType("JSP_SCRIPTLET", JspLanguage)
    val EXPRESSION = IElementType("JSP_EXPRESSION", JspLanguage)
    val DECLARATION = IElementType("JSP_DECLARATION", JspLanguage)
    val COMMENT = IElementType("JSP_COMMENT", JspLanguage)
    val EL = IElementType("JSP_EL", JspLanguage)
}

/** Separates server-side regions; the HTML layer handles all remaining text. */
class JspHighlightingLexer : LexerBase() {
    private var buffer: CharSequence = ""
    private var limit = 0
    private var start = 0
    private var end = 0
    private var type: IElementType? = null

    override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
        this.buffer = buffer
        limit = endOffset
        start = startOffset
        locateToken()
    }

    // Every server-side region is a single token, so all token boundaries are restartable.
    override fun getState(): Int = 0
    override fun getTokenType(): IElementType? = type
    override fun getTokenStart(): Int = start
    override fun getTokenEnd(): Int = end
    override fun getBufferSequence(): CharSequence = buffer
    override fun getBufferEnd(): Int = limit

    override fun advance() {
        start = end
        locateToken()
    }

    private fun locateToken() {
        if (start >= limit) {
            end = limit
            type = null
            return
        }
        when {
            matches(start, "<%--") -> {
                type = JspTokenTypes.COMMENT
                end = closingEnd(start + 4, "--%>")
            }
            matches(start, "<%") -> {
                type = when (if (start + 2 < limit) buffer[start + 2] else null) {
                    '@' -> JspTokenTypes.DIRECTIVE
                    '=' -> JspTokenTypes.EXPRESSION
                    '!' -> JspTokenTypes.DECLARATION
                    else -> JspTokenTypes.SCRIPTLET
                }
                end = closingEnd(start + 2, "%>")
            }
            isElStart(start) -> {
                type = JspTokenTypes.EL
                end = elEnd(start + 2)
            }
            else -> {
                type = JspTokenTypes.HTML_TEXT
                end = start + 1
                while (end < limit && !matches(end, "<%") && !isElStart(end)) end++
            }
        }
    }

    private fun matches(offset: Int, value: String): Boolean =
        offset + value.length <= limit && value.indices.all { buffer[offset + it] == value[it] }

    private fun closingEnd(from: Int, delimiter: String): Int {
        var offset = from
        while (offset + delimiter.length <= limit) {
            if (matches(offset, delimiter)) return offset + delimiter.length
            offset++
        }
        return limit
    }

    private fun isElStart(offset: Int): Boolean {
        if (offset + 1 >= limit || (buffer[offset] != '$' && buffer[offset] != '#') || buffer[offset + 1] != '{') {
            return false
        }
        var backslashes = 0
        var previous = offset - 1
        while (previous >= 0 && buffer[previous] == '\\') {
            backslashes++
            previous--
        }
        return backslashes % 2 == 0
    }

    private fun elEnd(from: Int): Int {
        var depth = 1
        var quote: Char? = null
        var offset = from
        while (offset < limit) {
            val character = buffer[offset++]
            if (quote != null) {
                when (character) {
                    '\\' -> if (offset < limit) offset++
                    quote -> quote = null
                }
            } else {
                when (character) {
                    '\'', '"' -> quote = character
                    '{' -> depth++
                    '}' -> if (--depth == 0) return offset
                }
            }
        }
        return limit
    }
}
