package com.look4counter.jsp

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.application.options.CodeStyle
import com.intellij.ide.highlighter.HtmlFileType

class JspDirectiveFormattingTest : BasePlatformTestCase() {
    fun testTopLevelDirectivesStayAtColumnZero() {
        assertEquals(example, format(example))
    }

    fun testExistingEightSpaceIndentIsRemoved() {
        val lines = example.lines()
        val source = lines.mapIndexed { index, line -> if (index in 1..2) "        $line" else line }.joinToString("\n")
        assertEquals(example, format(source))
        assertEquals(example, format(myFixture.editor.document.text))
    }

    fun testMultilineDirectivesAndCommentContentsArePreserved() {
        val directive = "<%@ page\n  contentType = 'text/html; charset=UTF-8'\n    pageEncoding='UTF-8' %>"
        val comment = "<%-- 한글\n  주석 --%>"
        val source = "$comment\n$directive\n\n<%@ taglib prefix='c' uri='jakarta.tags.core' %>\n<!doctype html>"
        val result = format(source)
        assertTrue(result, result.contains(directive))
        assertTrue(result, result.contains(comment))
        for (line in result.lines().filter { it.trimStart().startsWith("<%@") }) assertTrue(result, line.startsWith("<%@"))
        assertEquals(result, format(result))
    }

    fun testNestedHtmlDirectivesFollowHtmlIndent() {
        val source = "<div>\n<section>\n<%@ page import='demo.Widget' %>\n<%@ include file='piece.jspf' %>\n<p>본문</p>\n</section>\n</div>"
        val result = format(source)
        assertTrue(result, result.contains("\n    <section>"))
        assertTrue(result, result.contains("\n        <%@ page"))
        assertTrue(result, result.contains("\n        <%@ include"))
        assertTrue(result, result.contains("\n        <p>본문</p>"))
        assertEquals(result, format(result))
    }

    fun testHtmlIndentSettingsAreRespected() {
        val settings = CodeStyle.getSettings(project).clone()
        val indent = settings.getIndentOptions(HtmlFileType.INSTANCE)
        indent.INDENT_SIZE = 2
        indent.CONTINUATION_INDENT_SIZE = 6
        CodeStyle.setTemporarySettings(project, settings)
        try {
            val source = "<div>\n<%@ page import='demo.Widget' %>\n<%@ include file='piece.jspf' %>\n<p>본문</p>\n</div>"
            val result = format(source)
            assertTrue(result, result.contains("\n  <%@ page"))
            assertTrue(result, result.contains("\n  <%@ include"))
            assertTrue(result, result.contains("\n  <p>본문</p>"))
            assertEquals(example, format(example))
        } finally {
            CodeStyle.dropTemporarySettings(project)
        }
    }

    fun testSelectedDirectiveDoesNotChangeOutsideRange() {
        val prefix = "<%@ page import='demo.Widget' %>\n"
        val selected = "        <%@ include file='piece.jspf' %>"
        val suffix = "\n        <%@ page pageEncoding='UTF-8' %>\n<!doctype html>"
        myFixture.configureByText(JspFileType.INSTANCE, prefix + selected + suffix)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformatText(myFixture.file, prefix.length, prefix.length + selected.length)
        }
        assertEquals(prefix + selected.trimStart() + suffix, myFixture.editor.document.text)
    }

    fun testJspfFormattingIsStable() {
        myFixture.configureByText("header.jspf", example)
        WriteCommandAction.runWriteCommandAction(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        assertEquals(example, myFixture.editor.document.text)
    }

    private fun format(source: String): String {
        myFixture.configureByText(JspFileType.INSTANCE, source)
        WriteCommandAction.runWriteCommandAction(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        return myFixture.editor.document.text
    }

    private val example = "<%@ page import=\"java.text.SimpleDateFormat\" %>\n" +
        "<%@ page contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\" %>\n" +
        "<%@ include file=\"./include.jsp\" %>\n<!doctype html>"
}
