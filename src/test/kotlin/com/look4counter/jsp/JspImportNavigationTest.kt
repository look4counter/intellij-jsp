package com.look4counter.jsp

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.psi.PsiClass
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.DumbModeTestUtils
import java.nio.file.Files
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

class JspImportNavigationTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.addFileToProject("demo/Widget.java", "package demo; public class Widget {}")
        myFixture.addFileToProject("demo/Other.java", "package demo; public class Other {}")
    }

    fun testSingleClassNavigation() {
        configure("<%@ page import=\"demo.Wid<caret>get\" %>")
        assertClass("demo.Widget")
    }

    fun testCommaSeparatedClassesNavigateIndependently() {
        configure("<%@ page import='demo.Widget, demo.Other' %>")
        for (name in listOf("demo.Widget", "demo.Other")) {
            myFixture.editor.caretModel.moveToOffset(myFixture.file.text.indexOf(name) + name.lastIndexOf('.') + 2)
            assertClass(name)
        }
    }

    fun testWhitespaceQuotesAndJspf() {
        for (quote in listOf("'", "\"")) {
            configure("<%@ page import=$quote \n demo.Wid<caret>get , demo.Other \n $quote %>", "fragment.jspf")
            assertClass("demo.Widget")
            val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)!!
            assertEquals("demo.Widget", reference.rangeInElement.substring(reference.element.text))
        }
    }

    fun testRepeatedImportAttributesAndMultipleDirectives() {
        configure("<%@ page import='demo.Widget' import=\"demo.Other\" %><%@ page import='demo.Widget' %>")
        val directiveReferences = myFixture.file.children.filterIsInstance<JspDirective>().flatMap { it.references.toList() }
        assertEquals(listOf("demo.Widget", "demo.Other", "demo.Widget"), directiveReferences.map {
            it.rangeInElement.substring(it.element.text)
        })
        for (reference in directiveReferences) {
            myFixture.editor.caretModel.moveToOffset(reference.element.textOffset + reference.rangeInElement.startOffset + 7)
            assertClass(reference.canonicalText)
        }
    }

    fun testNestedAndUnicodeClasses() {
        myFixture.addFileToProject("demo/Outer.java", "package demo; public class Outer { public static class Inner {} }")
        myFixture.addFileToProject("demo/한글.java", "package demo; public class 한글 {}")
        configure("<%@ page import='demo.Outer.In<caret>ner' %>")
        assertClass("demo.Outer.Inner")
        configure("<%@ page import='demo.한<caret>글' %>")
        assertClass("demo.한글")
    }

    fun testLibraryClassNavigationWithoutSource() {
        val jar = Files.createTempFile("jsp-import-library", ".jar").toFile()
        jar.deleteOnExit()
        JarOutputStream(jar.outputStream()).use { output ->
            output.putNextEntry(JarEntry("junit/framework/Assert.class"))
            javaClass.getResourceAsStream("/junit/framework/Assert.class")!!.use { it.copyTo(output) }
            output.closeEntry()
        }
        PsiTestUtil.addLibrary(testRootDisposable, module, "navigation-library", jar.parent, jar.name)
        configure("<%@ page import='junit.framework.As<caret>sert' %>")
        assertClass("junit.framework.Assert")
        assertEquals("jar", targets().single().containingFile.virtualFile.fileSystem.protocol)
    }

    fun testUnresolvedWildcardAndMalformedNamesHaveNoClassTarget() {
        for (source in listOf("<%@ page import='missing.No<caret>SuchClass' %>",
            "<%@ page import='demo.<caret>*' %>", "<%@ page import='demo..Wid<caret>get' %>",
            "<%@ page import='demo.Wid<caret>get'", "<%@ page import='demo.Wid<caret>get %>")) {
            configure(source)
            assertTrue(source, targets().isEmpty())
        }
    }

    fun testOnlyNameRangesAreReferences() {
        configure("<%@ page import='  demo.Widget ,   demo.Other  ' contentType='demo.Widget' %>")
        val directive = myFixture.file.children.filterIsInstance<JspDirective>().single()
        val references = directive.references
        assertEquals(2, references.size)
        assertEquals(listOf("demo.Widget", "demo.Other"), references.map { it.rangeInElement.substring(directive.text) })
        for (offset in listOf(directive.text.indexOf("import"), directive.text.indexOf(',') + 2,
            directive.text.indexOf("contentType"), directive.text.lastIndexOf("demo.Widget") + 7)) {
            myFixture.editor.caretModel.moveToOffset(offset)
            assertTrue("Unexpected target at $offset", targets().isEmpty())
        }
    }

    fun testJspCommentAndOtherDirectiveAreNotImportLinks() {
        for (source in listOf("<%-- <%@ page import='demo.Wid<caret>get' %> --%>",
            "<%@ taglib uri='demo.Wid<caret>get' prefix='demo' %>")) {
            configure(source)
            assertTrue(source, targets().isEmpty())
        }
    }

    fun testReferenceIsNotResolvedDuringIndexing() {
        configure("<%@ page import='demo.Wid<caret>get' %>")
        val reference = myFixture.file.findReferenceAt(myFixture.caretOffset)!!
        DumbModeTestUtils.runInDumbModeSynchronously(project) {
            assertNull(reference.resolve())
        }
        assertClass("demo.Widget")
    }

    private fun configure(source: String, name: String = "navigation.jsp") {
        val offset = source.indexOf("<caret>")
        myFixture.configureByText(name, source.replace("<caret>", ""))
        if (offset >= 0) myFixture.editor.caretModel.moveToOffset(offset)
    }

    private fun targets() = GotoDeclarationAction.findAllTargetElements(project, myFixture.editor, myFixture.caretOffset)

    private fun assertClass(name: String) {
        val original = myFixture.editor.document.text
        val result = targets()
        assertEquals(result.toList().toString(), 1, result.size)
        assertEquals(name, (result.single() as? PsiClass)?.qualifiedName)
        assertEquals(original, myFixture.editor.document.text)
    }
}
