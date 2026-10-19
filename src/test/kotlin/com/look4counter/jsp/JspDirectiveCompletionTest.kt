package com.look4counter.jsp

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JspDirectiveCompletionTest : BasePlatformTestCase() {
    fun testDirectiveNames() {
        myFixture.configureByText(JspFileType.INSTANCE, "<%@ <caret> %>")
        val names = myFixture.completeBasic().orEmpty().map { it.lookupString }
        assertContainsElements(names, "page", "include", "taglib")
    }

    fun testPageAttributeInsertion() {
        select("<%@ page con<caret> %><div>한글</div>", "contentType")
        assertEquals("<%@ page contentType=\"\" %><div>한글</div>", myFixture.editor.document.text)
        assertEquals(myFixture.editor.document.text.indexOf("\"\"") + 1, myFixture.editor.caretModel.offset)
    }

    fun testBooleanValueInsertion() {
        select("<%@ page session=\"tr<caret>\" %>", "true")
        assertEquals("<%@ page session=\"true\" %>", myFixture.editor.document.text)
    }

    fun testIncompleteDirectiveNameWithoutSpace() {
        select("<%@pa<caret>", "page")
        assertEquals("<%@page ", myFixture.editor.document.text)
    }

    fun testIncludeAttributeInsertion() {
        select("<%@ include fi<caret> %>", "file")
        assertEquals("<%@ include file=\"\" %>", myFixture.editor.document.text)
    }

    fun testExistingEqualsAndQuotesArePreserved() {
        select("<%@ page con<caret> = 'text/html' %>", "contentType")
        assertEquals("<%@ page contentType = 'text/html' %>", myFixture.editor.document.text)
        assertEquals(myFixture.editor.document.text.indexOf("text/html"), myFixture.editor.caretModel.offset)
    }

    fun testEncodingValueAndSingleQuotedBoolean() {
        select("<%@ page pageEncoding=\"UTF<caret>\" %>", "UTF-8")
        assertEquals("<%@ page pageEncoding=\"UTF-8\" %>", myFixture.editor.document.text)
        select("<%@ page isELIgnored='fa<caret>' %>", "false")
        assertEquals("<%@ page isELIgnored='false' %>", myFixture.editor.document.text)
    }

    fun testUnquotedValueGetsQuotes() {
        select("<%@ page session=tr<caret> %>", "true")
        assertEquals("<%@ page session=\"true\" %>", myFixture.editor.document.text)
    }

    fun testTaglibAttributesAndMutualExclusion() {
        myFixture.configureByText(JspFileType.INSTANCE, "<%@ taglib prefix='c' uri='example' <caret> %>")
        val names = myFixture.completeBasic().orEmpty().map { it.lookupString }
        assertFalse(names.toString(), "prefix" in names || "uri" in names || "tagdir" in names)
        select("<%@ taglib pre<caret> %>", "prefix")
        assertEquals("<%@ taglib prefix=\"\" %>", myFixture.editor.document.text)
    }

    fun testDuplicateAttributesExceptImportAreOmitted() {
        myFixture.configureByText(JspFileType.INSTANCE, "<%@ page contentType='text/html' import='demo.Helper' <caret> %>")
        val names = myFixture.completeBasic().orEmpty().map { it.lookupString }
        assertFalse(names.toString(), "contentType" in names)
        assertTrue(names.toString(), "import" in names)
        assertTrue(names.toString(), "pageEncoding" in names)
    }

    fun testNoDirectiveCandidatesOutsideDirectives() {
        for (source in listOf("<%-- pa<caret> --%>", "<div title='pa<caret>'></div>", "\${pa<caret>}", "<% int pa<caret> %>")) {
            myFixture.configureByText(JspFileType.INSTANCE, source)
            val names = myFixture.completeBasic().orEmpty().map { it.lookupString }
            assertFalse(names.toString(), names.any { it in setOf("pageEncoding", "taglib", "contentType") })
        }
    }

    private fun select(source: String, candidate: String) {
        myFixture.configureByText(JspFileType.INSTANCE, source)
        val variants = myFixture.completeBasic()
        myFixture.lookup?.let { lookup ->
            val item = variants.orEmpty().firstOrNull { it.lookupString == candidate }
            assertNotNull("Missing $candidate in ${variants?.map { it.lookupString }}", item)
            lookup.currentItem = item
            myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
        }
        assertTrue(myFixture.editor.document.text, myFixture.editor.document.text.contains(candidate))
    }
}
