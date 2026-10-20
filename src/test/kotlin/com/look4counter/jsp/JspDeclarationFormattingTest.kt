package com.look4counter.jsp

import com.intellij.application.options.CodeStyle
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JspDeclarationFormattingTest : BasePlatformTestCase() {
    fun testNestedDeclarationUsesHtmlParentAndJavaBodyIndent() {
        val source = "<div>\n<section>\n<%!\npublic int count(int n){if(n>0){return n+1;}return 0;}\n%>\n</section>\n</div>"
        val result = format(source)
        assertTrue(result, result.contains("\n        <%!\n            public int count(int n) {"))
        assertTrue(result, result.contains("\n                if (n > 0) {\n                    return n + 1;"))
        assertTrue(result, result.contains("\n        %>\n    </section>"))
        assertEquals(result, format(result))
    }

    fun testJavaAndHtmlCodeStyleAreAppliedIndependently() {
        val settings = CodeStyle.getSettings(project).clone()
        settings.getIndentOptions(HtmlFileType.INSTANCE).INDENT_SIZE = 2
        settings.getIndentOptions(JavaFileType.INSTANCE).INDENT_SIZE = 3
        settings.getCommonSettings(JavaLanguage.INSTANCE).METHOD_BRACE_STYLE = 2
        settings.getCommonSettings(JavaLanguage.INSTANCE).SPACE_AROUND_ADDITIVE_OPERATORS = false
        CodeStyle.setTemporarySettings(project, settings)
        try {
            val result = format("<div>\n<%!\npublic int sum(int a,int b){return a+b;}\n%>\n</div>")
            assertTrue(result, result.contains("\n  <%!\n     public int sum(int a, int b)\n     {\n        return a+b;\n     }\n  %>"))
            assertEquals(result, format(result))
        } finally { CodeStyle.dropTemporarySettings(project) }
    }

    fun testJavaTabIndent() {
        val settings = CodeStyle.getSettings(project).clone()
        val indent = settings.getIndentOptions(JavaFileType.INSTANCE)
        indent.USE_TAB_CHARACTER = true
        indent.TAB_SIZE = 4
        indent.INDENT_SIZE = 4
        CodeStyle.setTemporarySettings(project, settings)
        try {
            val result = format("<%!\npublic int value(){return 1;}\n%>")
            assertEquals("<%!\n\tpublic int value() {\n\t\treturn 1;\n\t}\n%>", result)
            assertEquals(result, format(result))
        } finally { CodeStyle.dropTemporarySettings(project) }
    }

    fun testMultipleEmptyAndUnfinishedDeclarations() {
        val result = format("<%@ page language='java' %>\n        <%!\n int first=1;\n %>\n<%!\n  \n   %>\n<%!\n int second=2;\n %>")
        assertTrue(result, result.contains("\n<%!\n    int first = 1;\n%>"))
        assertTrue(result, result.contains("\n<%!\n%>\n<%!\n    int second = 2;\n%>"))
        assertEquals(result, format(result))
        val unfinished = "<%!\n public int value(){return 1;"
        val formatted = format(unfinished)
        assertEquals(unfinished.filterNot(Char::isWhitespace), formatted.filterNot(Char::isWhitespace))
        assertEquals(formatted, format(formatted))
    }

    fun testLiteralAndCommentContentsArePreserved() {
        val comment = "/* 한글\n   원문  주석 */"
        val literal = "\"한글  \\n\""
        val textBlock = "\"\"\"\n  앞  뒤\n    다음\n  \"\"\""
        val source = "<%!\n$comment\nString greeting(){return $literal;}\nString block=$textBlock;\n%>"
        val result = format(source)
        assertTrue(result, result.contains(comment))
        assertTrue(result, result.contains(literal))
        assertTrue(result, result.contains(textBlock))
        assertEquals(result, format(result))
    }

    fun testWholeSelectedDeclarationPreservesOutside() {
        val prefix = "<%@ page language = 'java' %>\n"
        val selected = "<%!\n public int value(){return 1;}\n      %>"
        val suffix = "\n<div   id = 'outside'> untouched </div>"
        myFixture.configureByText(JspFileType.INSTANCE, prefix + selected + suffix)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformatText(myFixture.file, prefix.length, prefix.length + selected.length)
        }
        assertEquals(prefix + "<%!\n    public int value() {\n        return 1;\n    }\n%>" + suffix,
            myFixture.editor.document.text)
    }

    fun testPartialDeclarationSelectionPreservesOutside() {
        val prefix = "<%!\n        int before=1;\n        "
        val selected = "int selected=2;"
        val suffix = "\n         int after=3;\n                %>"
        myFixture.configureByText(JspFileType.INSTANCE, prefix + selected + suffix)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformatText(myFixture.file, prefix.length, prefix.length + selected.length)
        }
        assertEquals(prefix + "int selected = 2;" + suffix, myFixture.editor.document.text)
    }

    fun testSelectedIndentedEmptyDeclarationKeepsValidRange() {
        val prefix = "<%@ page language='java' %>\n"
        val selected = "                        <%!\n  %>"
        val suffix = "\n<%@ include file='outside.jspf' %>"
        myFixture.configureByText(JspFileType.INSTANCE, prefix + selected + suffix)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformatText(myFixture.file, prefix.length, prefix.length + selected.length)
        }
        assertEquals(prefix + "<%!\n%>" + suffix, myFixture.editor.document.text)
    }

    fun testReportedDeclarationAndFunctionIndent() {
        val source = "<%@ page contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\" %>\n" +
            "        <%!\n            public String test() {\n            return \"Hello\";\n        }\n                %>\n" +
            "<div>\n    Hello\n</div>"
        val expected = "<%@ page contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\" %>\n" +
            "<%!\n    public String test() {\n        return \"Hello\";\n    }\n%>\n<div>\n    Hello\n</div>"
        val result = format(source)
        assertEquals(expected, result)
        assertEquals(result, format(result))
    }

    private fun format(source: String): String {
        myFixture.configureByText(JspFileType.INSTANCE, source)
        WriteCommandAction.runWriteCommandAction(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        return myFixture.editor.document.text
    }
}
