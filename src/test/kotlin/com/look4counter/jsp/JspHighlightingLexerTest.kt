package com.look4counter.jsp

import com.intellij.psi.tree.IElementType
import org.junit.Assert.*
import org.junit.Test

class JspHighlightingLexerTest {
    @Test
    fun `separates every JSP block kind from HTML`() {
        val samples = listOf(
            "<%@ page language=\"java\" %>" to JspTokenTypes.DIRECTIVE,
            "<% int value = 1; %>" to JspTokenTypes.SCRIPTLET,
            "<%= value %>" to JspTokenTypes.EXPRESSION,
            "<%! int value; %>" to JspTokenTypes.DECLARATION,
            "<%-- <script>ignored</script> --%>" to JspTokenTypes.COMMENT,
            "\${value}" to JspTokenTypes.EL,
            "#{value}" to JspTokenTypes.EL,
        )
        for ((sample, type) in samples) {
            val text = "<h1>$sample</h1>"
            val tokens = tokens(text)
            assertEquals(listOf(JspTokenTypes.HTML_TEXT, type, JspTokenTypes.HTML_TEXT), tokens.map { it.type })
            assertEquals(sample, tokens[1].text)
        }
    }

    @Test
    fun `EL balances nested braces and ignores braces in quoted strings`() {
        val expression = "\${{'name': '}', 'nested': {'x': \"\\\"}\"}}}"
        val tokens = tokens("before$expression after")
        assertEquals(expression, tokens[1].text)
        assertSame(JspTokenTypes.EL, tokens[1].type)
        assertEquals(" after", tokens[2].text)
    }

    @Test
    fun `escaped EL remains HTML while an even number of backslashes allows EL`() {
        assertEquals(listOf(JspTokenTypes.HTML_TEXT), tokens("\\\${value} \\#{other}").map { it.type })
        assertEquals(listOf(JspTokenTypes.HTML_TEXT, JspTokenTypes.EL), tokens("\\\\\${value}").map { it.type })
    }

    @Test
    fun `unfinished blocks extend to the end without gaps`() {
        for (sample in listOf("<%", "<%@ page", "<%= value", "<%! int n", "<%-- comment", "\${{'key': 'value'}", "#{value")) {
            val tokens = tokens("before$sample")
            assertEquals(sample, tokens.last().text)
            assertNotSame(JspTokenTypes.HTML_TEXT, tokens.last().type)
        }
        assertTrue(tokens("").isEmpty())
        assertEquals("한글 < > $ #", tokens("한글 < > $ #").single().text)
    }

    @Test
    fun `JSP comments consume fake delimiters and embedded HTML`() {
        val comment = "<%-- <%= x %> \${foo} <style> --%>"
        assertEquals(listOf(comment, "<p>after</p>"), tokens("$comment<p>after</p>").map { it.text })
    }

    @Test
    fun `lexer restarts at every token boundary`() {
        val text = "<a title=\"\${value}\"><%= name %><%-- comment --%>한글</a>"
        val expected = tokens(text)
        var offset = 0
        for (index in expected.indices) {
            val lexer = JspHighlightingLexer()
            lexer.start(text, offset, text.length, 0)
            assertEquals(expected.drop(index), collect(lexer))
            offset += expected[index].text.length
        }
    }

    @Test
    fun `lexer respects the supplied buffer limit`() {
        val lexer = JspHighlightingLexer()
        val text = "xx<%= value %>tail"
        lexer.start(text, 2, 8, 0)
        assertEquals(listOf(Token(JspTokenTypes.EXPRESSION, "<%= va")), collect(lexer))
        assertEquals(8, lexer.bufferEnd)
        assertSame(text, lexer.bufferSequence)
    }

    private data class Token(val type: IElementType, val text: String)

    private fun tokens(text: String): List<Token> = collect(JspHighlightingLexer().apply { start(text) })

    private fun collect(lexer: JspHighlightingLexer): List<Token> {
        val result = mutableListOf<Token>()
        while (lexer.tokenType != null) {
            assertTrue("Every token must consume input", lexer.tokenEnd > lexer.tokenStart)
            result += Token(lexer.tokenType!!, lexer.bufferSequence.subSequence(lexer.tokenStart, lexer.tokenEnd).toString())
            lexer.advance()
        }
        return result
    }
}
