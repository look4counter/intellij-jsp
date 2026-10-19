package com.look4counter.jsp

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JspIncludeContextTest : BasePlatformTestCase() {
    fun testIncludedPublicValueMethodInRealHtmlPage() {
        val page = """
            <%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
            <%@ include file="include.jsp" %>
            <!doctype html>
            <html lang="ko">
            <head><meta charset="UTF-8"><title>Tomcat Server 테스트</title></head>
            <body><main class="card">
                <h1>Tomcat 배포 테스트</h1>
                <div class="info"><span>서버 응답 시각</span><strong><%= te<caret>() %></strong></div>
                <form action="hello" method="get"><input name="name"><button>Servlet 테스트</button></form>
            </main></body></html>
        """.trimIndent()
        myFixture.addFileToProject("WebContent/include.jsp", """
            <%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
                    <%!
                public String test() {
                    return "테스트 성공";
                }
                    %>
            <div>Hello</div>
        """.trimIndent())
        complete("WebContent/index.jsp", page, "test")
    }

    fun testIncludedPublicMethodCompletionWithoutParentheses() {
        myFixture.addFileToProject("WebContent/include.jsp", "<%! public String test() { return \"성공\"; } %>")
        val source = "<%@ include file=\"include.jsp\" %><strong><%= te %></strong>"
        configure("WebContent/index.jsp", source)
        val document = myFixture.editor.document
        myFixture.editor.caretModel.moveToOffset(source.indexOf("te %>") + 2)
        val variants = myFixture.completeBasic()
        myFixture.lookup?.let { lookup ->
            val item = variants.orEmpty().firstOrNull { it.lookupString == "test" }
            assertNotNull("Missing test in ${variants?.map { it.lookupString }}; Java: ${injectedJava().text}", item)
            lookup.currentItem = item
            myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
        }
        assertTrue(document.text, document.text.contains("<%= test() %>"))
    }

    fun testIncludedVoidMethodCompletionInScriptlet() {
        myFixture.addFileToProject("WebContent/include.jsp", "<%! public void test() { } %>")
        complete("WebContent/index.jsp", "<%@ include file='include.jsp' %><% te<caret>(); %>", "test")
    }

    fun testMismatchedIncludeFilenameAndCorrectedMethodCompletion() {
        myFixture.addFileToProject("WebContent/incldue.jsp", "<%! public String test() { return \"Hello\"; } %>")
        configure("WebContent/index.jsp", "<%@ include file=\"./include.jsp\" %><strong><%= 1 %></strong>")
        assertFalse(injectedJava().text.contains("public String test()"))
        complete("WebContent/corrected.jsp", "<%@ include file=\"./incldue.jsp\" %><strong><%= te<caret>() %></strong>", "test")
    }

    fun testIncludedVariableAndMethodCompletion() {
        myFixture.addFileToProject("parts/common.jspf", "<%! int includedMethod() { return 1; } %><% int includedCount = 2; %>")
        complete("main.jsp", "<%@ include file='parts/common.jspf' %><%= includedCou<caret> %>", "includedCount")
        complete("method.jsp", "<%@ include file='parts/common.jspf' %><%= includedMet<caret>() %>", "includedMethod")
    }

    fun testNestedRelativeIncludesAndImports() {
        myFixture.addFileToProject("demo/Widget.java", "package demo; public class Widget { public int widgetCount; }")
        myFixture.addFileToProject("common.jspf", "<%@ page import='demo.Widget' %><% Widget widget = null; %>")
        myFixture.addFileToProject("parts/first.jspf", "<%@ include file='../common.jspf' %>")
        complete("main.jsp", "<%@ include file='parts/first.jspf' %><%= widget.widgetCou<caret> %>", "widgetCount")
    }

    fun testWebRootInclude() {
        myFixture.addFileToProject("src/main/webapp/parts/common.jspf", "<% int rootCount = 1; %>")
        complete("src/main/webapp/views/main.jsp", "<%@ include file='/parts/common.jspf' %><%= rootCou<caret> %>", "rootCount")
    }

    fun testInsertionOrderAndSplitControlFlow() {
        myFixture.addFileToProject("open.jspf", "<% if (true) { int insideCount = 1; %>")
        myFixture.addFileToProject("close.jspf", "<% } %>")
        configure("main.jsp", "<% int before = 1; %><%@ include file='open.jspf' %><%= insideCount %><%@ include file='close.jspf' %>")
        val java = injectedJava()
        assertTrue(java.text, java.text.indexOf("int before") < java.text.indexOf("if (true)"))
        assertTrue(java.text, PsiTreeUtil.collectElementsOfType(java, PsiErrorElement::class.java).isEmpty())
    }

    fun testLaterIncludedLocalIsNotVisibleBeforeInclude() {
        myFixture.addFileToProject("later.jspf", "<% int futureCount = 1; %>")
        configure("main.jsp", "<% int current = 1; %><%= futureCount %><%@ include file='later.jspf' %>")
        val java = injectedJava()
        val reference = PsiTreeUtil.collectElementsOfType(java, com.intellij.psi.PsiReferenceExpression::class.java)
            .first { it.referenceName == "futureCount" }
        assertNull(reference.resolve())
    }

    fun testUniqueParentContextInFragment() {
        myFixture.addFileToProject("main.jsp", "<% int parentCount = 1; %><%@ include file='piece.jspf' %>")
        complete("piece.jspf", "<%= parentCou<caret> %>", "parentCount")
    }

    fun testMultipleParentsUseStandaloneContext() {
        myFixture.addFileToProject("a.jsp", "<% int firstParent = 1; %><%@ include file='piece.jspf' %>")
        myFixture.addFileToProject("b.jsp", "<% int secondParent = 1; %><%@ include file='piece.jspf' %>")
        configure("piece.jspf", "<% int ownCount = 1; %>")
        val java = injectedJava()
        assertFalse(java.text, java.text.contains("firstParent"))
        assertFalse(java.text, java.text.contains("secondParent"))
        assertTrue(java.text, java.text.contains("ownCount"))
    }

    fun testDynamicIncludeAndCommentsDoNotMerge() {
        myFixture.addFileToProject("other.jspf", "<% int hiddenCount = 1; %>")
        configure("main.jsp", "<%-- <%@ include file='other.jspf' %> --%><jsp:include page='other.jspf'/><% int ownCount = 1; %>")
        assertFalse(injectedJava().text.contains("hiddenCount"))
    }

    fun testNestedUniqueParentContext() {
        myFixture.addFileToProject("main.jsp", "<% int outerCount = 1; %><%@ include file='middle.jspf' %>")
        myFixture.addFileToProject("middle.jspf", "<%@ include file='leaf.jspf' %>")
        complete("leaf.jspf", "<%= outerCou<caret> %>", "outerCount")
    }

    fun testRepeatedFragmentUsesStandaloneContext() {
        myFixture.addFileToProject("main.jsp", "<% int parentOnly = 1; %><%@ include file='piece.jspf' %><%@ include file='piece.jspf' %>")
        configure("piece.jspf", "<% int ownCount = 1; %>")
        assertFalse(injectedJava().text.contains("parentOnly"))
    }

    fun testIncludedDocumentEditsAreReflectedWithoutSaving() {
        val included = myFixture.addFileToProject("common.jspf", "<% int oldCount = 1; %>")
        configure("main.jsp", "<%@ include file='common.jspf' %><%= 1 %>")
        assertTrue(injectedJava().text.contains("oldCount"))
        replaceText(included, "<% int newCount = 2; %>")
        val updated = injectedJava().text
        assertTrue(updated, updated.contains("newCount"))
        assertFalse(updated, updated.contains("oldCount"))
    }

    fun testCreateDeleteAndDirectiveEditsAreReflected() {
        configure("main.jsp", "<%@ include file='missing.jspf' %><%= 1 %>")
        assertFalse(injectedJava().text.contains("addedCount"))
        val added = myFixture.addFileToProject("missing.jspf", "<% int addedCount = 1; %>")
        assertTrue(injectedJava().text.contains("addedCount"))
        WriteCommandAction.runWriteCommandAction(project) { added.virtualFile.delete(this) }
        assertFalse(injectedJava().text.contains("addedCount"))
        myFixture.addFileToProject("replacement.jspf", "<% int replacementCount = 1; %>")
        val host = InjectedLanguageManager.getInstance(project).getTopLevelFile(myFixture.file)
        replaceText(host, "<%@ include file='replacement.jspf' %><%= 1 %>")
        assertTrue(injectedJava().text.contains("replacementCount"))
    }

    fun testAddingSecondParentInvalidatesUniqueContext() {
        myFixture.addFileToProject("first.jsp", "<% int parentCount = 1; %><%@ include file='piece.jspf' %>")
        configure("piece.jspf", "<%= 1 %>")
        assertTrue(injectedJava().text.contains("parentCount"))
        val second = myFixture.addFileToProject("second.jsp", "<%@ include file='piece.jspf' %>")
        assertFalse(injectedJava().text.contains("parentCount"))
        replaceText(second, "<p>No include</p>")
        assertTrue(injectedJava().text.contains("parentCount"))
    }

    fun testCycleMissingAndUnfinishedIncludes() {
        myFixture.addFileToProject("cycle.jspf", "<%@ include file='main.jsp' %><% int cycleCount = 1; %>")
        configure("main.jsp", "<%@ include file='cycle.jspf' %><%@ include file='missing.jspf' %><%= cycleCount %><%@ include file='")
        val java = injectedJava().text
        assertEquals(java, 1, Regex("int cycleCount").findAll(java).count())
        assertTrue(java.length < 2000)
    }

    fun testFormattingAndCompletionPreserveIncludedDocument() {
        val source = "<%! int  sharedValue=1; %><% int sharedCount=2; %>"
        val included = myFixture.addFileToProject("common.jspf", source)
        complete("main.jsp", "<%@ include file='common.jspf' %><%= sharedCou<caret> %>", "sharedCount")
        val main = InjectedLanguageManager.getInstance(project).getTopLevelFile(myFixture.file)
        WriteCommandAction.runWriteCommandAction(project) { CodeStyleManager.getInstance(project).reformat(main) }
        assertEquals(source, included.text)
        assertFalse(main.text.contains("int sharedCount"))
    }

    fun testFragmentFormattingPreservesParent() {
        val source = "<% int parentCount=1; %><%@ include file='piece.jspf' %>"
        val parent = myFixture.addFileToProject("main.jsp", source)
        configure("piece.jspf", "<% int local=parentCount+1; %>")
        WriteCommandAction.runWriteCommandAction(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        assertEquals(source, parent.text)
        assertTrue(myFixture.file.text, myFixture.file.text.contains("local = parentCount + 1"))
        assertFalse(myFixture.file.text.contains("int parentCount"))
    }

    fun testDeepIncludeChainIsBounded() {
        for (index in 0..69) {
            val next = if (index < 69) "<%@ include file='level${index + 1}.jspf' %>" else ""
            myFixture.addFileToProject("level$index.jspf", "<% int level_$index = $index; %>$next")
        }
        configure("main.jsp", "<%@ include file='level0.jspf' %><%= 1 %>")
        val java = injectedJava().text
        assertTrue(java, java.contains("int level_63"))
        assertFalse(java, java.contains("int level_64"))
    }

    fun testBlockScopedIncludedVariableDoesNotLeak() {
        myFixture.addFileToProject("scope.jspf", "<% if (true) { int scopedCount = 1; } %>")
        configure("main.jsp", "<%@ include file='scope.jspf' %><%= scopedCount %>")
        val reference = PsiTreeUtil.collectElementsOfType(injectedJava(), com.intellij.psi.PsiReferenceExpression::class.java)
            .first { it.referenceName == "scopedCount" }
        assertNull(reference.resolve())
    }

    private fun replaceText(file: PsiFile, text: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            val manager = PsiDocumentManager.getInstance(project)
            val document = manager.getDocument(file)!!
            document.setText(text)
            manager.commitDocument(document)
        }
    }

    private fun configure(path: String, text: String) {
        val file = myFixture.addFileToProject(path, text)
        myFixture.configureFromExistingVirtualFile(file.virtualFile)
    }

    private fun complete(path: String, source: String, candidate: String) {
        val offset = source.indexOf("<caret>")
        configure(path, source.replace("<caret>", ""))
        val hostDocument = myFixture.editor.document
        myFixture.editor.caretModel.moveToOffset(offset)
        val variants = myFixture.completeBasic()
        myFixture.lookup?.let { lookup ->
            val item = variants.orEmpty().firstOrNull { it.lookupString == candidate }
            assertNotNull("Missing $candidate in ${variants?.map { it.lookupString }}", item)
            lookup.currentItem = item
            myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
        }
        val prefix = source.substring(0, offset).takeLastWhile { it.isJavaIdentifierPart() }
        val expected = source.replace("${prefix}<caret>", candidate)
        assertTrue("Completion not inserted in host: ${hostDocument.text}",
            hostDocument.text.contains(candidate) && !hostDocument.text.contains("<caret>"))
        assertEquals(expected.filterNot(Char::isWhitespace), hostDocument.text.filterNot(Char::isWhitespace))
    }

    private fun injectedJava(): PsiJavaFile {
        val file = InjectedLanguageManager.getInstance(project).getTopLevelFile(myFixture.file)
        val host = file.children.filterIsInstance<JspJavaInjectionHost>().last()
        return InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(host)!!.first().first as PsiJavaFile
    }
}
