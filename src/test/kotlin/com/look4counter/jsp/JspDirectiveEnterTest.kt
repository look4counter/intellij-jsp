package com.look4counter.jsp

import com.intellij.openapi.actionSystem.IdeActions
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.openapi.command.undo.UndoManager
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider
import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.openapi.util.Ref
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.openapi.command.WriteCommandAction

class JspDirectiveEnterTest : BasePlatformTestCase() {
    private var oldSmartIndent = true

    override fun setUp() {
        super.setUp()
        oldSmartIndent = CodeInsightSettings.getInstance().SMART_INDENT_ON_ENTER
        CodeInsightSettings.getInstance().SMART_INDENT_ON_ENTER = true
    }

    override fun tearDown() {
        try {
            CodeInsightSettings.getInstance().SMART_INDENT_ON_ENTER = oldSmartIndent
        } finally {
            super.tearDown()
        }
    }
    fun testUserDirectiveExampleAtEndOfFile() {
        for (directive in directives) {
            myFixture.configureByText(JspFileType.INSTANCE, "$directive<caret>")
            enter()
            myFixture.checkResult("$directive\n<caret>")
            assertEquals("Unexpected virtual indent", 0, myFixture.editor.caretModel.logicalPosition.column)
        }
    }

    fun testEnterBetweenUserExampleDirectives() {
        val source = directives.joinToString("\n")
        myFixture.configureByText(JspFileType.INSTANCE, source.replaceFirst("%>", "%><caret>"))
        enter()
        myFixture.checkResult(source.replaceFirst("%>", "%>\n<caret>"))
    }

    fun testPhysicalFileDirectiveEnter() {
        val source = directives.joinToString("\n")
        val file = myFixture.addFileToProject("index.jsp", source)
        myFixture.configureFromExistingVirtualFile(file.virtualFile)
        myFixture.editor.caretModel.moveToOffset(source.length)
        enter()
        assertEquals(source + "\n", myFixture.editor.document.text)
        assertEquals("Unexpected virtual indent", 0, myFixture.editor.caretModel.logicalPosition.column)
    }

    fun testExistingSpacesAndTabsArePreserved() {
        for (indent in listOf("    ", "\t", " \t  ")) {
            myFixture.configureByText("indent.jspf", "$indent${directives.first()}<caret>")
            enter()
            myFixture.checkResult("$indent${directives.first()}\n$indent<caret>")
        }
    }

    fun testTaglibAndTrailingSpaces() {
        val directive = "<%@ taglib prefix='c' uri='jakarta.tags.core' %>"
        myFixture.configureByText(JspFileType.INSTANCE, "$directive  <caret>")
        enter()
        myFixture.checkResult("$directive  \n<caret>")
    }

    fun testMultilineDirectiveUsesStartingIndent() {
        val source = "  <%@ page\n      import='demo.Widget' %>"
        myFixture.configureByText(JspFileType.INSTANCE, "$source<caret>")
        enter()
        myFixture.checkResult("$source\n  <caret>")
    }

    fun testRepeatedEnterAndUndo() {
        val source = directives.first()
        myFixture.configureByText(JspFileType.INSTANCE, "$source<caret>")
        enter()
        UndoManager.getInstance(project).undo(TextEditorProvider.getInstance().getTextEditor(myFixture.editor))
        myFixture.checkResult("$source<caret>")
        enter()
        enter()
        myFixture.checkResult("$source\n\n<caret>")
    }

    fun testOtherContextsAreDelegatedWithoutEditing() {
        for ((name, source) in listOf(
            "inline.jsp" to "<div><%@ page import='demo.Widget' %><caret></div>",
            "unfinished.jsp" to "<%@ page import='demo.Widget'<caret>",
            "scriptlet.jsp" to "<% int n = 1; %><caret>",
            "java-body.jsp" to "<% if (true) {<caret> %>",
            "plain.html" to "<div><caret></div>",
            "Plain.java" to "class Plain {<caret>}",
        )) {
            myFixture.configureByText(name, source)
            val before = myFixture.editor.document.text
            val result = JspDirectiveEnterHandler().preprocessEnter(myFixture.file, myFixture.editor,
                Ref(myFixture.caretOffset), Ref(0), SimpleDataContext.getProjectContext(project), null)
            assertEquals(name, EnterHandlerDelegate.Result.Continue, result)
            assertEquals(before, myFixture.editor.document.text)
        }
    }

    fun testHtmlEnterMatchesNativeHtml() {
        val source = "<div><caret></div>"
        myFixture.configureByText("plain.html", source)
        enter()
        val expected = myFixture.editor.document.text
        val expectedPosition = myFixture.editor.caretModel.logicalPosition
        myFixture.configureByText("plain.jsp", source)
        enter()
        assertEquals(expected, myFixture.editor.document.text)
        assertEquals(expectedPosition, myFixture.editor.caretModel.logicalPosition)
    }

    fun testRegisteredHandlerStopsSubsequentIndentCalculation() {
        val handler = EnterHandlerDelegate.EP_NAME.extensionList.filterIsInstance<JspDirectiveEnterHandler>().single()
        val source = directives.first()
        myFixture.configureByText(JspFileType.INSTANCE, "$source<caret>")
        WriteCommandAction.runWriteCommandAction(project) {
            val result = handler.preprocessEnter(myFixture.file, myFixture.editor, Ref(myFixture.caretOffset),
                Ref(0), SimpleDataContext.getProjectContext(project), null)
            assertEquals(EnterHandlerDelegate.Result.Stop, result)
        }
        myFixture.checkResult("$source\n<caret>")
    }

    private fun enter() = myFixture.performEditorAction(IdeActions.ACTION_EDITOR_ENTER, null)

    private val directives = listOf(
        "<%@ page import=\"java.text.SimpleDateFormat\" %>",
        "<%@ page contentType=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\" %>",
        "<%@ include file=\"./include.jsp\" %>",
    )
}
