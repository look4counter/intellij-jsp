package com.look4counter.jsp

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.templateLanguages.OuterLanguageElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JspCompletionTest : BasePlatformTestCase() {
    fun testHtmlPsiPreservesOriginalTextAndOffsets() {
        val source = "<%@ page language=\"java\" %><div title=\"앞\${name}뒤\"><%= title %></div>"
        myFixture.configureByText(JspFileType.INSTANCE, source)
        val provider = myFixture.file.viewProvider
        val html = provider.getPsi(HTMLLanguage.INSTANCE)
        assertNotNull("JSP must expose an HTML PSI root", html)
        assertEquals(source, html!!.text)
        assertEquals(source.length, html.textLength)
        assertFalse(provider.findElementAt(source.indexOf("div")) is OuterLanguageElement)
        assertSame(HTMLLanguage.INSTANCE, provider.findElementAt(source.indexOf("div"))!!.containingFile.language)
        assertSame(JspLanguage, provider.findElementAt(source.indexOf("page"))!!.language)
        assertSame(JspLanguage, provider.findElementAt(source.indexOf("name"))!!.language)
        assertEquals(source, myFixture.file.copy().text)
    }

    fun testHtmlTagCompletionAfterJspDirectiveAndComment() {
        completeAndSelect("<%@ page language=\"java\" %><%-- 한글 --%><di<caret>", "div")
        assertTrue(myFixture.editor.document.text.startsWith("<%@ page language=\"java\" %><%-- 한글 --%><div"))
    }

    fun testHtmlAttributeCompletionPreservesJspAndFollowingText() {
        val prefix = "<%= title %><div "
        val suffix = " data-value=\"\${name}\">한글</div>"
        completeAndSelect("${prefix}cl<caret>$suffix", "class")
        val result = myFixture.editor.document.text
        assertTrue(result, result.startsWith("${prefix}class"))
        assertTrue(result, result.endsWith(suffix))
    }

    fun testHtmlCompletionAfterDelimiterEdits() {
        myFixture.configureByText(JspFileType.INSTANCE, "<%-- comment --%><di<caret>")
        val document = myFixture.editor.document
        val delimiter = document.text.indexOf("--%>")
        WriteCommandAction.runWriteCommandAction(project) { document.deleteString(delimiter, delimiter + 4) }
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        WriteCommandAction.runWriteCommandAction(project) { document.insertString(delimiter, "--%>") }
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        chooseCompletion("div")
        assertTrue(document.text, document.text.startsWith("<%-- comment --%><div"))
    }

    fun testNoHtmlCompletionInsideJspOrUnfinishedBlocks() {
        for (source in listOf("", "<% int cl<caret> %>", "<%-- <di<caret>", "\${cl<caret>")) {
            myFixture.configureByText(JspFileType.INSTANCE, source)
            val before = myFixture.editor.document.text
            val variants = myFixture.completeBasic()
            val names = variants?.map { it.lookupString }.orEmpty()
            assertFalse(names.toString(), "div" in names || "class" in names)
            assertEquals(before, myFixture.editor.document.text)
        }
    }

    fun testJavaScriptLocalVariableCompletionWithJspBeforeAndInsideScript() {
        if (!java.lang.Boolean.getBoolean("jsp.test.web.languages")) return
        val prefix = "<%@ page language=\"java\" %><script>const greetingMessage = \"앞<%= title %>뒤\"; "
        completeAndSelect("${prefix}greetingMes<caret>;</script>", "greetingMessage")
        assertEquals("${prefix}greetingMessage;</script>", myFixture.editor.document.text)
    }

    fun testCssPropertyCompletionWithJspBeforeAndInsideStyle() {
        if (!java.lang.Boolean.getBoolean("jsp.test.web.languages")) return
        val prefix = "<%-- 한글 --%><style>.card { content: \"앞\${name}뒤\"; "
        completeAndSelect("${prefix}col<caret> }</style>", "color")
        val result = myFixture.editor.document.text
        assertTrue(result, result.startsWith("${prefix}color"))
        assertTrue(result, result.endsWith("}</style>"))
    }

    private fun completeAndSelect(source: String, candidate: String) {
        myFixture.configureByText(JspFileType.INSTANCE, source)
        chooseCompletion(candidate)
    }

    private fun chooseCompletion(candidate: String) {
        val variants = myFixture.complete(CompletionType.BASIC, 1)
        val lookup = myFixture.lookup
        if (lookup == null) {
            assertTrue("Expected completion $candidate", myFixture.editor.document.text.contains(candidate))
            return
        }
        val item = variants?.firstOrNull { it.lookupString == candidate }
        assertNotNull("Missing $candidate in ${variants?.map { it.lookupString }}", item)
        lookup.currentItem = item
        myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
    }
}
