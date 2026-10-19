package com.look4counter.jsp

import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.PsiDocumentManager
import com.intellij.application.options.CodeStyle
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JspFormattingTest : BasePlatformTestCase() {
    fun testHtmlFormattingMatchesExistingFormatter() {
        val source = "<html>\n<body>\n<div>\n<span>hello</span>\n</div>\n</body>\n</html>"
        val expected = format(source, HtmlFileType.INSTANCE)
        assertFalse("HTML example must require formatting", source == expected)
        assertEquals(expected, format(source))
    }

    fun testHtmlCodeStyleSettingsAreReused() {
        val settings = CodeStyle.getSettings(project).clone()
        val indent = settings.getIndentOptions(HtmlFileType.INSTANCE)
        indent.INDENT_SIZE = 3
        indent.CONTINUATION_INDENT_SIZE = 3
        CodeStyle.setTemporarySettings(project, settings)
        try {
            val source = "<div>\n<section>\n<p>text</p>\n</section>\n</div>"
            val expected = format(source, HtmlFileType.INSTANCE)
            assertTrue(expected, expected.contains("\n   <section>"))
            assertEquals(expected, format(source))
        } finally {
            CodeStyle.dropTemporarySettings(project)
        }
    }

    fun testSelectedRangePreservesOutsideCode() {
        val prefix = "<div>\n  <p   id = 'outside'>outside</p>\n"
        val selected = "<section>\n<span   id = 'inside'>inside</span>\n</section>"
        val suffix = "\n  <p   id = 'after'>after</p>\n</div>"
        myFixture.configureByText(JspFileType.INSTANCE, prefix + selected + suffix)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformatText(myFixture.file, prefix.length, prefix.length + selected.length)
        }
        val result = myFixture.editor.document.text
        assertTrue(result, result.startsWith(prefix))
        assertTrue(result, result.endsWith(suffix))
        assertFalse(result == prefix + selected + suffix)
    }

    fun testJavaScriptFormatterIsReused() {
        if (!java.lang.Boolean.getBoolean("jsp.test.web.languages")) return
        val source = "<script>\nfunction hello(){let count=1;return count+2;}\n</script>"
        val expected = format(source, HtmlFileType.INSTANCE)
        assertFalse(source == expected)
        assertTrue(expected, expected.contains("count = 1"))
        assertEquals(expected, format(source))
    }

    fun testCssFormatterIsReused() {
        if (!java.lang.Boolean.getBoolean("jsp.test.web.languages")) return
        val source = "<style>\n.card{color:red;margin:0  2px;}\n</style>"
        val expected = format(source, HtmlFileType.INSTANCE)
        assertFalse(source == expected)
        assertTrue(expected, expected.contains("color: red"))
        assertEquals(expected, format(source))
    }

    fun testServerConstructsPreserveContentsExceptJavaWhitespace() {
        val tokens = listOf(
            "<%@ page  language = \"java\" %>", "<%-- comment\n  한글 --%>",
            "<% if (true) {\n  out.print(\"x\"); } %>", "<%=  title  %>",
            "<%! int  count = 0; %>", "\${ user.name }", "#{ value['key'] }",
        )
        val source = tokens.first() + "\n<div>\n<section>\n" + tokens.drop(1).joinToString("\n") +
            "\n<span>text</span>\n</section>\n</div>"
        val result = format(source)
        assertEquals(tokens.map(::normalizeJava), serverTokens(result).map(::normalizeJava))
        assertTrue(result, result.contains("    <section>"))
        assertEquals(result, format(result))
    }

    fun testServerConstructsInsideHtmlAndWebContentArePreserved() {
        val source = """
            <div title="앞${'$'}{ user.name }뒤" data-id="<%=  id  %>">
            <p>앞<%=  title  %>뒤 ${'$'}{value}</p>
            <script>
            const value="앞<%= title %>뒤"; function f(){return value;}
            const multiline=`<%= title +
              suffix %>`;
            </script>
            <style>
            .card{content:"앞${'$'}{ name }뒤";color:red;}
            /* <%= color +
              suffix %> */
            </style>
            </div>
        """.trimIndent()
        val result = format(source)
        assertEquals(serverTokens(source).map(::normalizeJava), serverTokens(result).map(::normalizeJava))
        assertEquals(source.filterNot { it.isWhitespace() }, result.filterNot { it.isWhitespace() })
        assertEquals(result, format(result))
    }

    fun testWhitespaceSensitiveHtmlMatchesExistingFormatter() {
        val source = "<div>앞 <span>가</span> 뒤</div>\n<pre>  a\n    b  </pre>"
        assertEquals(format(source, HtmlFileType.INSTANCE), format(source))
    }

    fun testEmptyAndUnfinishedServerConstructsDoNotLoseText() {
        for (source in listOf("", "<%@ page  ", "<%-- a\n b", "\${ value", "<div>\n<% if (true) {\n  ")) {
            val result = format(source)
            assertEquals(serverTokens(source).map(::normalizeJava), serverTokens(result).map(::normalizeJava))
            assertEquals(source.filterNot { it.isWhitespace() }, result.filterNot { it.isWhitespace() })
            assertEquals(result, format(result))
        }
    }

    private fun normalizeJava(token: String): String =
        if (token.startsWith("<%") && !token.startsWith("<%@") && !token.startsWith("<%--"))
            token.filterNot(Char::isWhitespace) else token

    private fun serverTokens(source: String): List<String> {
        val lexer = JspHighlightingLexer()
        lexer.start(source)
        val result = mutableListOf<String>()
        while (lexer.tokenType != null) {
            if (lexer.tokenType != JspTokenTypes.HTML_TEXT) result += source.substring(lexer.tokenStart, lexer.tokenEnd)
            lexer.advance()
        }
        return result
    }

    private fun format(source: String, fileType: FileType = JspFileType.INSTANCE): String {
        myFixture.configureByText(fileType, source)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformat(myFixture.file)
        }
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        return myFixture.editor.document.text
    }
}
