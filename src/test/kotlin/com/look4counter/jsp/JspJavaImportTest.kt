package com.look4counter.jsp

import com.intellij.codeInsight.lookup.Lookup
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiJavaCodeReferenceElement
import com.intellij.psi.PsiClass
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.daemon.QuickFixActionRegistrar
import com.intellij.codeInsight.daemon.HighlightDisplayKey
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider
import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JspJavaImportTest : BasePlatformTestCase() {
    override fun setUp() {
        super.setUp()
        myFixture.addFileToProject("demo/Widget.java", "package demo; public class Widget { public Widget() {} }")
    }

    fun testCompletionAddsImportInAllJavaRegions() {
        for (source in listOf("<% Widg<caret> value; %>", "<%! Widg<caret> value; %>", "<%= new Widg<caret> %>")) {
            myFixture.configureByText(JspFileType.INSTANCE, source)
            val document = hostDocument()
            val items = myFixture.completeBasic()
            myFixture.lookup?.let { lookup ->
                lookup.currentItem = items!!.first { it.lookupString == "Widget" }
                myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
            }
            assertTrue(document.text, document.text.contains("<%@ page import=\"demo.Widget\" %>"))
            assertFalse(document.text, document.text.contains("demo.Widget value"))
        }
    }

    fun testQuickFixAddsImport() {
        myFixture.configureByText(JspFileType.INSTANCE, "<% Wid<caret>get value; %>")
        val document = hostDocument()
        val action = actions().single { it.text == "JSP import 추가: demo.Widget" }
        assertTrue(action.isAvailable(project, myFixture.editor, myFixture.file))
        WriteCommandAction.runWriteCommandAction(project) { action.invoke(project, myFixture.editor, myFixture.file) }
        assertEquals("<%@ page import=\"demo.Widget\" %>\n<% Widget value; %>", document.text)
        val editor = (myFixture.editor as? com.intellij.injected.editor.EditorWindow)?.delegate ?: myFixture.editor
        UndoManager.getInstance(project).undo(TextEditorProvider.getInstance().getTextEditor(editor))
        assertEquals("<% Widget value; %>", document.text)
    }

    fun testConstructorParenthesesAndCaret() {
        val result = complete("<%= new Widg<caret> %>")
        assertTrue(result, result.contains("new Widget()"))
        val editor = (myFixture.editor as? com.intellij.injected.editor.EditorWindow)?.delegate ?: myFixture.editor
        assertEquals(result.indexOf("Widget()") + "Widget()".length, editor.caretModel.offset)
    }

    fun testQuickFixPreservesTypeArgumentsAndSelectedPackage() {
        myFixture.addFileToProject("other/Widget.java", "package other; public class Widget<T> {}")
        myFixture.configureByText(JspFileType.INSTANCE, "<%! Wid<caret>get<String> value; %>")
        val document = hostDocument()
        val action = actions().single { it.text == "JSP import 추가: other.Widget" }
        WriteCommandAction.runWriteCommandAction(project) { action.invoke(project, myFixture.editor, myFixture.file) }
        assertEquals("<%@ page import=\"other.Widget\" %>\n<%! Widget<String> value; %>", document.text)
    }

    fun testExistingImportsPreserveOriginalText() {
        for (directive in listOf("<%@ page import='demo.Widget' %>", "<%@ page import=\"demo.*\" %>")) {
            val source = "$directive\n<div>한글</div><% Widg<caret> value; %>"
            assertEquals(source.replace("Widg<caret>", "Widget"), complete(source))
        }
    }

    fun testConflictingExplicitAndWildcardImportsUseQualifiedName() {
        myFixture.addFileToProject("other/Widget.java", "package other; public class Widget {}")
        for (import in listOf("other.Widget", "other.*")) {
            val source = "<%@ page import='$import' %><% Widget before; Widg<caret> after; %>"
            val result = complete(source, "demo.Widget")
            assertEquals(source.replace("Widg<caret>", "demo.Widget"), result)
        }
    }

    fun testIncludeImportIsReusedAndParentIsUnchanged() {
        val parent = myFixture.addFileToProject("parent.jsp", "<%@ page import='demo.Widget' %><%@ include file='part.jspf' %>")
        val fragment = myFixture.addFileToProject("part.jspf", "<% Widg value; %>")
        myFixture.configureFromExistingVirtualFile(fragment.virtualFile)
        myFixture.editor.caretModel.moveToOffset(fragment.text.indexOf("Widg") + 4)
        val document = hostDocument()
        chooseCompletion("demo.Widget")
        assertEquals("<% Widget value; %>", document.text)
        assertEquals("<%@ page import='demo.Widget' %><%@ include file='part.jspf' %>", parent.text)
    }

    fun testCopiedIncludeReferenceDoesNotOfferQuickFix() {
        myFixture.addFileToProject("declaration.jspf", "<%! Widget included; %>")
        val page = myFixture.addFileToProject("page.jsp", "<%@ include file='declaration.jspf' %><% int local = 1; %>")
        myFixture.configureFromExistingVirtualFile(page.virtualFile)
        val file = InjectedLanguageManager.getInstance(project).getTopLevelFile(myFixture.file)
        val host = file.children.filterIsInstance<JspJavaInjectionHost>().single()
        val java = InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(host)!!.first().first
        val copied = PsiTreeUtil.collectElementsOfType(java, PsiJavaCodeReferenceElement::class.java)
            .first { it.text == "Widget" }
        assertNull(JspJavaImports.reference(copied))
    }

    fun testQuickFixCandidatesDistinguishPackagesAndExcludeInaccessibleClass() {
        myFixture.addFileToProject("other/Widget.java", "package other; public class Widget {}")
        myFixture.addFileToProject("hidden/Widget.java", "package hidden; class Widget {}")
        myFixture.configureByText(JspFileType.INSTANCE, "<%! Wid<caret>get value; %>")
        val names = actions().map { it.text }.filter { it.startsWith("JSP import 추가:") }
        assertTrue(names.toString(), "JSP import 추가: demo.Widget" in names)
        assertTrue(names.toString(), "JSP import 추가: other.Widget" in names)
        assertFalse(names.toString(), "JSP import 추가: hidden.Widget" in names)
    }

    fun testQuickFixIsAbsentOutsideClassReferences() {
        for (source in listOf("<div>Wid<caret>get</div>", "<% // Wid<caret>get\n %>",
            "<%= \"Wid<caret>get\" %>", "<%-- Wid<caret>get --%>", "\${Wid<caret>get}")) {
            myFixture.configureByText(JspFileType.INSTANCE, source)
            assertTrue(source, actions().none { it.text.startsWith("JSP import 추가:") })
        }
    }

    fun testCompletionResolvesAndUndoRestoresOriginalInOneStep() {
        val source = "<div>한글</div><% Widg<caret> value; %>"
        complete(source)
        PsiDocumentManager.getInstance(project).commitAllDocuments()
        val host = InjectedLanguageManager.getInstance(project).getTopLevelFile(myFixture.file)
        val injectionHost = host.children.filterIsInstance<JspJavaInjectionHost>().single()
        val java = InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(injectionHost)!!.first().first
        val reference = PsiTreeUtil.collectElementsOfType(java, PsiJavaCodeReferenceElement::class.java)
            .first { it.text == "Widget" }
        assertEquals("demo.Widget", (reference.resolve() as? PsiClass)?.qualifiedName)
        val editor = (myFixture.editor as? com.intellij.injected.editor.EditorWindow)?.delegate ?: myFixture.editor
        val textEditor = TextEditorProvider.getInstance().getTextEditor(editor)
        UndoManager.getInstance(project).undo(textEditor)
        assertEquals(source.replace("<caret>", ""), editor.document.text)
    }

    private fun complete(source: String, qualifiedName: String = "demo.Widget"): String {
        myFixture.configureByText(JspFileType.INSTANCE, source)
        val document = hostDocument()
        chooseCompletion(qualifiedName)
        return document.text
    }

    private fun chooseCompletion(qualifiedName: String) {
        val items = myFixture.completeBasic()
        myFixture.lookup?.let { lookup ->
            lookup.currentItem = items!!.first { (it.psiElement as? PsiClass)?.qualifiedName == qualifiedName }
            myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
        }
    }

    private fun hostDocument() = PsiDocumentManager.getInstance(project).getDocument(
        InjectedLanguageManager.getInstance(project).getTopLevelFile(myFixture.file))!!

    private fun actions(): List<IntentionAction> {
        val element = myFixture.file.findElementAt(myFixture.caretOffset) ?: return emptyList()
        val ref = PsiTreeUtil.getParentOfType(element, PsiJavaCodeReferenceElement::class.java, false) ?: return emptyList()
        val actions = mutableListOf<IntentionAction>()
        val future = com.intellij.openapi.application.ReadAction.nonBlocking<Unit> {
            UnresolvedReferenceQuickFixProvider.registerReferenceFixes(ref, object : QuickFixActionRegistrar {
                override fun register(action: IntentionAction) { actions += action }
                override fun register(range: TextRange, action: IntentionAction, key: HighlightDisplayKey?) { actions += action }
            })
        }.submit(com.intellij.util.concurrency.AppExecutorUtil.getAppExecutorService())
        com.intellij.testFramework.PlatformTestUtil.waitForPromise(future)
        return actions
    }
}
