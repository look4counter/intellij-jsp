package com.look4counter.jsp

import com.intellij.codeInsight.navigation.actions.GotoDeclarationAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JspNavigationTest : BasePlatformTestCase() {
    fun testIncludePathDeclarationNavigation() {
        val target = myFixture.addFileToProject("parts/common.jspf", "<%! int value; %>")
        configure("index.jsp", "<%@ include file=\"parts/com<caret>mon.jspf\" %>")
        val resolved = targets()
        assertEquals(1, resolved.size)
        assertEquals(target.virtualFile, resolved.single().containingFile.virtualFile)
    }

    fun testIncludedMethodNavigatesToOriginalIdentifier() {
        val source = "<%! public String test() { return \"Hello\"; } %>"
        val target = myFixture.addFileToProject("include.jsp", source)
        configure("index.jsp", "<%@ include file='include.jsp' %><strong><%= te<caret>st() %></strong>")
        val resolved = targets()
        assertEquals(1, resolved.size)
        assertEquals(target.virtualFile, resolved.single().containingFile.virtualFile)
        assertEquals(source.indexOf("test"), resolved.single().textOffset)
    }

    fun testNestedIncludeOverloadNavigationAndEdit() {
        val source = "<%! public int test() { return 1; } public int test(int n) { return n; } %>"
        val target = myFixture.addFileToProject("parts/functions.jspf", source)
        myFixture.addFileToProject("middle.jspf", "<%@ include file='parts/functions.jspf' %>")
        configure("index.jsp", "<%@ include file='middle.jspf' %><%= te<caret>st(1) %>")
        assertEquals(source.indexOf("test(int"), targets().single().textOffset)
        assertEquals(target.virtualFile, targets().single().containingFile.virtualFile)
        WriteCommandAction.runWriteCommandAction(project) {
            val manager = PsiDocumentManager.getInstance(project)
            val document = manager.getDocument(target)!!
            document.insertString(0, "<%-- 한글 --%>\n")
            manager.commitDocument(document)
        }
        assertEquals(target.text.indexOf("test(int"), targets().single().textOffset)
    }

    fun testParentRelativeAndWebRootPaths() {
        val target = myFixture.addFileToProject("WebContent/parts/common.jspf", "<p>한글</p>")
        configure("WebContent/views/relative.jsp", "<%@ include file='../parts/co<caret>mmon.jspf' %>")
        assertEquals(target.virtualFile, targets().single().containingFile.virtualFile)
        configure("WebContent/views/absolute.jsp", "<%@ include file='/parts/co<caret>mmon.jspf' %>")
        assertEquals(target.virtualFile, targets().single().containingFile.virtualFile)
    }

    fun testMissingAndUnfinishedPathsHaveNoTarget() {
        configure("missing-index.jsp", "<%@ include file='miss<caret>ing.jsp' %>")
        assertTrue(targets().isEmpty())
        configure("unfinished.jsp", "<%@ include file='miss<caret>ing.jsp")
        assertTrue(targets().isEmpty())
    }

    fun testSameFileMethodNavigatesToActualName() {
        val source = "<%! int ownMethod() { return 1; } %><%= ownMet<caret>hod() %>"
        configure("same.jsp", source)
        val file = myFixture.file
        val target = targets().single()
        assertEquals(file.virtualFile, target.containingFile.virtualFile)
        assertEquals(source.indexOf("ownMethod"), target.textOffset)
    }

    fun testOrdinaryJavaMethodKeepsPlatformNavigation() {
        val java = myFixture.addFileToProject("demo/Helper.java", "package demo; public class Helper { public static int work() { return 1; } }")
        configure("java.jsp", "<%= demo.Helper.wo<caret>rk() %>")
        assertEquals(java.virtualFile, targets().single().containingFile.virtualFile)
    }

    fun testAmbiguousParentMethodDoesNotResolveToWrongPage() {
        myFixture.addFileToProject("a.jsp", "<%! int onlyA() { return 1; } %><%@ include file='piece.jspf' %>")
        myFixture.addFileToProject("b.jsp", "<%! int onlyB() { return 2; } %><%@ include file='piece.jspf' %>")
        configure("piece.jspf", "<%= on<caret>lyA() %>")
        assertTrue(targets().isEmpty())
    }

    private fun configure(path: String, text: String) {
        val offset = text.indexOf("<caret>")
        val file = myFixture.addFileToProject(path, text.replace("<caret>", ""))
        myFixture.configureFromExistingVirtualFile(file.virtualFile)
        myFixture.editor.caretModel.moveToOffset(offset)
    }

    private fun targets() = GotoDeclarationAction.findAllTargetElements(project, myFixture.editor, myFixture.editor.caretModel.offset)
}
