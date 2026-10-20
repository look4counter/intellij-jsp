package com.look4counter.jsp

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.DumbModeTestUtils
import com.intellij.testFramework.PsiTestUtil
import java.nio.file.Files
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate

class JspDirectiveValueCompletionTest : BasePlatformTestCase() {
    private var fileIndex = 0
    fun testImportClassCompletion() {
        myFixture.addFileToProject("demo/Widget.java", "package demo; public class Widget {}")
        select("<%@ page import='Wid<caret>' %>", "demo.Widget")
        assertEquals("<%@ page import='demo.Widget' %>", myFixture.editor.document.text)
    }

    fun testIncludeFileCompletion() {
        myFixture.addFileToProject("header.jspf", "<p>header</p>")
        select("<%@ include file='hea<caret>' %>", "header.jspf")
        assertEquals("<%@ include file='header.jspf' %>", myFixture.editor.document.text)
    }

    fun testImportQualifiedPrefixListAndSuffixPreserved() {
        myFixture.addFileToProject("demo/Widget.java", "package demo; public class Widget {}")
        val source = "<%@ page import=\"demo.Other,  demo.Wid<caret>get, demo.Last\" pageEncoding='UTF-8' %><div>한글</div>"
        select(source, "demo.Widget")
        assertEquals(source.replace("demo.Wid<caret>get", "demo.Widget"), myFixture.editor.document.text)
    }

    fun testImportUnquotedValueGetsQuotes() {
        myFixture.addFileToProject("demo/Widget.java", "package demo; public class Widget {}")
        select("<%@ page import=Wid<caret> %>", "demo.Widget")
        assertEquals("<%@ page import=\"demo.Widget\" %>", myFixture.editor.document.text)
    }

    fun testFixedValueCompletionReplacesExistingSuffix() {
        select("<%@ page session='tr<caret>ue' %>", "true")
        assertEquals("<%@ page session='true' %>", myFixture.editor.document.text)
    }

    fun testPackageAndNestedClassCompletion() {
        myFixture.addFileToProject("demo/Outer.java", "package demo; public class Outer { public static class Inner {} }")
        select("<%@ page import='de<caret>' %>", "demo")
        assertEquals("<%@ page import='demo.' %>", myFixture.editor.document.text)
        select("<%@ page import='demo.Outer.In<caret>' %>", "demo.Outer.Inner")
        assertEquals("<%@ page import='demo.Outer.Inner' %>", myFixture.editor.document.text)
    }

    fun testLibraryClassCompletion() {
        val jar = Files.createTempFile("jsp-directive-completion", ".jar").toFile()
        jar.deleteOnExit()
        JarOutputStream(jar.outputStream()).use { output ->
            output.putNextEntry(JarEntry("junit/framework/Assert.class"))
            javaClass.getResourceAsStream("/junit/framework/Assert.class")!!.use { it.copyTo(output) }
            output.closeEntry()
        }
        PsiTestUtil.addLibrary(testRootDisposable, module, "directive-library", jar.parent, jar.name)
        select("<%@ page import='junit.framework.As<caret>' %>", "junit.framework.Assert")
        assertEquals("<%@ page import='junit.framework.Assert' %>", myFixture.editor.document.text)
    }

    fun testInaccessibleClassesAndMissingDirectoriesAreNotSuggested() {
        myFixture.addFileToProject("hidden/Invisible.java", "package hidden; class Invisible {}")
        myFixture.configureByText(JspFileType.INSTANCE, "<%@ page import='Invis<caret>' %>")
        assertTrue(myFixture.completeBasic().orEmpty().none { it.lookupString == "hidden.Invisible" })
        myFixture.configureByText(JspFileType.INSTANCE, "<%@ include file='missing/he<caret>' %>")
        assertTrue(myFixture.completeBasic().orEmpty().isEmpty())
    }

    fun testFolderThenFileCompletionPreservesParentPath() {
        myFixture.addFileToProject("parts/header.jspf", "header")
        select("<%@ include file=\"pa<caret>\" %>", "parts/")
        assertEquals("<%@ include file=\"parts/\" %>", myFixture.editor.document.text)
        select("<%@ include file=\"parts/hea<caret>der.jspf\" %>", "header.jspf")
        assertEquals("<%@ include file=\"parts/header.jspf\" %>", myFixture.editor.document.text)
    }

    fun testPunctuationSelectionDoesNotDuplicateSeparators() {
        myFixture.addFileToProject("demo/Widget.java", "package demo; public class Widget {}")
        myFixture.addFileToProject("delta/Other.java", "package delta; public class Other {}")
        myFixture.addFileToProject("parts/header.jspf", "header")
        myFixture.addFileToProject("pages/footer.jsp", "footer")
        select("<%@ page import='de<caret>' %>", "demo", completionChar = '.')
        assertEquals("<%@ page import='demo.' %>", myFixture.editor.document.text)
        select("<%@ include file='pa<caret>' %>", "parts/", completionChar = '/')
        assertEquals("<%@ include file='parts/' %>", myFixture.editor.document.text)
    }

    fun testParentAndWebRootPaths() {
        myFixture.addFileToProject("WebContent/parts/header.jspf", "header")
        select("<%@ include file='../parts/he<caret>' %>", "header.jspf", "WebContent/views/a.jsp")
        assertEquals("<%@ include file='../parts/header.jspf' %>", myFixture.editor.document.text)
        select("<%@ include file='/parts/he<caret>' %>", "header.jspf", "WebContent/views/b.jsp")
        assertEquals("<%@ include file='/parts/header.jspf' %>", myFixture.editor.document.text)
    }

    fun testAutoPopupContractIsLimitedToDirectives() {
        val handler = TypedHandlerDelegate.EP_NAME.extensionList.filterIsInstance<JspDirectiveAutoPopupHandler>().single()
        myFixture.configureByText(JspFileType.INSTANCE, "<%@ page import='demo.<caret>' %>")
        for (char in "@=\"'.,/") assertEquals("Popup for $char", TypedHandlerDelegate.Result.STOP,
            handler.checkAutoPopup(char, project, myFixture.editor, myFixture.file))
        myFixture.configureByText(JspFileType.INSTANCE, "<%<caret>")
        assertEquals(TypedHandlerDelegate.Result.STOP, handler.checkAutoPopup('@', project, myFixture.editor, myFixture.file))
        for (source in listOf("<%-- co<caret>mment --%>", "<div title='a<caret>'/>", "<%@ page import='demo.Widget' %><caret>")) {
            myFixture.configureByText(JspFileType.INSTANCE, source)
            assertEquals(TypedHandlerDelegate.Result.CONTINUE, handler.checkAutoPopup('.', project, myFixture.editor, myFixture.file))
        }
    }

    fun testIndexingSkipsClassSearchButKeepsFixedValues() {
        myFixture.addFileToProject("demo/Widget.java", "package demo; public class Widget {}")
        myFixture.configureByText(JspFileType.INSTANCE, "<%@ page import='Wid<caret>' %>")
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            assertTrue(myFixture.completeBasic().orEmpty().none { it.lookupString == "demo.Widget" })
        }
        myFixture.configureByText(JspFileType.INSTANCE, "<%@ page session='<caret>' %>")
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            assertContainsElements(myFixture.completeBasic().orEmpty().map { it.lookupString }, "true", "false")
        }
    }

    private fun select(source: String, candidate: String, path: String = "index${fileIndex++}.jsp", completionChar: Char = Lookup.NORMAL_SELECT_CHAR) {
        val offset = source.indexOf("<caret>")
        val file = myFixture.addFileToProject(path, source.replace("<caret>", ""))
        myFixture.configureFromExistingVirtualFile(file.virtualFile)
        myFixture.editor.caretModel.moveToOffset(offset)
        val variants = myFixture.completeBasic()
        myFixture.lookup?.let { lookup ->
            lookup.currentItem = variants.orEmpty().firstOrNull { it.lookupString == candidate }
                ?: error("Missing $candidate: ${variants.orEmpty().map { it.lookupString }}")
            myFixture.finishLookup(completionChar)
        }
    }
}
