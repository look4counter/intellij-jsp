package com.look4counter.jsp

import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.command.WriteCommandAction

class JspDirectiveAppearanceTest : BasePlatformTestCase() {
    fun testDirectivePartsHaveSeparateColorRoles() {
        val source = "<%@ page import=\"demo.Widget\" %>"
        val highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, JspFileType.INSTANCE)
        highlighter.setText(source)
        val keys = listOf(0, source.indexOf("page"), source.indexOf("import"), source.indexOf('='), source.indexOf("demo"))
            .map { highlighter.createIterator(it).textAttributesKeys.toList() }
        assertEquals(keys.toString(), 5, keys.toSet().size)
    }

    fun testUnfinishedStringsDoNotConsumeFollowingDirectives() {
        val source = "<%@ page import='unfinished %>\n<%@ include file=\"header.jsp\" %>"
        val highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, JspFileType.INSTANCE)
        highlighter.setText(source)
        assertTrue(JspSyntaxHighlighter.DIRECTIVE_NAME in highlighter.createIterator(source.indexOf("include")).textAttributesKeys)
        assertTrue(JspSyntaxHighlighter.DIRECTIVE_ATTRIBUTE in highlighter.createIterator(source.indexOf("file=")).textAttributesKeys)
        assertTrue(JspSyntaxHighlighter.DIRECTIVE_STRING in highlighter.createIterator(source.indexOf("header")).textAttributesKeys)
        val iterator = highlighter.createIterator(0)
        var end = 0
        while (!iterator.atEnd()) {
            assertEquals(end, iterator.start)
            assertTrue(iterator.end > iterator.start)
            end = iterator.end
            iterator.advance()
        }
        assertEquals(source.length, end)
    }

    fun testIncrementalDirectiveEditsMatchFreshHighlighting() {
        myFixture.configureByText(JspFileType.INSTANCE, "<%@ page import='demo.Widget' %>\n<div/><%@ include file=\"x.jsp\" %>")
        val editor = myFixture.editor as EditorEx
        for (replacement in listOf("\"unfinished", "'demo.Other'")) {
            val start = editor.document.text.indexOf("import=") + 7
            val end = editor.document.text.indexOf(" %>")
            WriteCommandAction.runWriteCommandAction(project) { editor.document.replaceString(start, end, replacement) }
            val fresh = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, JspFileType.INSTANCE)
            fresh.setText(editor.document.text)
            for (offset in editor.document.text.indices) {
                assertEquals("Token at $offset", fresh.createIterator(offset).tokenType, editor.highlighter.createIterator(offset).tokenType)
                assertEquals(fresh.createIterator(offset).textAttributesKeys.toList(), editor.highlighter.createIterator(offset).textAttributesKeys.toList())
            }
        }
    }

    fun testDirectiveColorKeysFollowAllAvailableThemes() {
        val source = "<%@\n page contentType='text/html' %>"
        val highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, JspFileType.INSTANCE)
        highlighter.setText(source)
        for (scheme in EditorColorsManager.getInstance().allSchemes) {
            highlighter.setColorScheme(scheme)
            assertTrue(JspSyntaxHighlighter.DIRECTIVE_NAME in highlighter.createIterator(source.indexOf("page")).textAttributesKeys)
            assertTrue(JspSyntaxHighlighter.DIRECTIVE_STRING in highlighter.createIterator(source.indexOf("text/html")).textAttributesKeys)
        }
    }
}
