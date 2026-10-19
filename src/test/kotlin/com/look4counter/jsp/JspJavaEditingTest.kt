package com.look4counter.jsp

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.ide.highlighter.JavaHighlightingColors
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.application.options.CodeStyle
import com.intellij.lang.java.JavaLanguage

class JspJavaEditingTest : BasePlatformTestCase() {
    fun testFragmentsShareJavaContext() {
        val source = "<%@ page import=\"java.util.List,java.util.Map\" %>" +
            "<%! String title() { return \"가\"; } %>" +
            "<% String name = title(); if (name != null) { %><div><%= name.length() %></div><% } %>"
        myFixture.configureByText(JspFileType.INSTANCE, source)
        val hosts = myFixture.file.children.filterIsInstance<JspJavaInjectionHost>()
        assertEquals(4, hosts.size)
        val injected = InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(hosts.last())
        assertNotNull("Expected Java injection", injected)
        val java = injected!!.first().first as PsiJavaFile
        assertTrue(java.text, java.text.contains("import java.util.List;"))
        assertTrue(java.text, java.text.contains("String name = title();"))
        assertTrue(java.text, PsiTreeUtil.collectElementsOfType(java, PsiErrorElement::class.java).isEmpty())
        assertEquals(source, myFixture.editor.document.text)
    }

    fun testDeclaredMemberCompletion() {
        myFixture.configureByText(JspFileType.INSTANCE, "<%! int greetingCount; %><%= greetingCou<caret> %>")
        val hostDocument = hostDocument()
        val variants = myFixture.completeBasic()
        assertTrue(variants?.map { it.lookupString }.toString(),
            hostDocument.text.endsWith("<%= greetingCount %>") || variants.orEmpty().any { it.lookupString == "greetingCount" })
    }

    fun testLocalVariableCompletionAcrossFragments() {
        myFixture.configureByText(JspFileType.INSTANCE,
            "<% int localCount = 1; if (true) { %><div><%= localCou<caret> %></div><% } %>")
        val hostDocument = hostDocument()
        val variants = myFixture.completeBasic()
        assertTrue(variants?.map { it.lookupString }.toString(),
            hostDocument.text.contains("<%= localCount %>") || variants.orEmpty().any { it.lookupString == "localCount" })
    }

    fun testCompletionInsertionPreservesSurroundingMarkup() {
        val prefix = "<% int localCount = 1; %><div title=\"앞"
        val suffix = "뒤\">한글</div>"
        myFixture.configureByText(JspFileType.INSTANCE, "$prefix<%= localCou<caret> %>$suffix")
        val hostDocument = hostDocument()
        val variants = myFixture.completeBasic()
        myFixture.lookup?.let { lookup ->
            val item = variants.orEmpty().firstOrNull { it.lookupString == "localCount" }
            assertNotNull(item)
            lookup.currentItem = item
            myFixture.finishLookup(Lookup.NORMAL_SELECT_CHAR)
        }
        assertEquals("$prefix<%= localCount %>$suffix", hostDocument.text)
    }

    fun testDeclarationAfterStatementsKeepsLocalScope() {
        myFixture.configureByText(JspFileType.INSTANCE,
            "<% int counter = 1; if (counter > 0) { %><%! int value; %><%= counter %><% } %>")
        val java = injectedJava()
        assertTrue(java.text, PsiTreeUtil.collectElementsOfType(java, PsiErrorElement::class.java).isEmpty())
        assertTrue(java.text, java.text.indexOf("int value;") < java.text.indexOf("void __jspService"))
    }

    fun testProjectTypeAndPageImportCompletion() {
        myFixture.addFileToProject("demo/Widget.java", "package demo; public class Widget { public int widgetCount; }")
        myFixture.configureByText(JspFileType.INSTANCE,
            "<%@ page import='demo.Widget' %><% Widget widget = new Widget(); %><%= widget.widgetCou<caret> %>")
        val variants = myFixture.completeBasic()
        assertTrue(variants?.map { it.lookupString }.toString(),
            myFixture.editor.document.text.contains("widgetCount") || variants.orEmpty().any { it.lookupString == "widgetCount" })
    }

    fun testServletImplicitObjectCompletion() {
        myFixture.addFileToProject("jakarta/servlet/http/HttpServletRequest.java",
            "package jakarta.servlet.http; public interface HttpServletRequest { String getParameter(String name); }")
        myFixture.configureByText(JspFileType.INSTANCE, "<%= request.getParam<caret> %>")
        val variants = myFixture.completeBasic()
        assertTrue(variants?.map { it.lookupString }.toString(),
            myFixture.editor.document.text.contains("getParameter") || variants.orEmpty().any { it.lookupString == "getParameter" })
    }

    fun testCompletionInsideScriptletAndDeclaration() {
        myFixture.addFileToProject("demo/Widget.java", "package demo; public class Widget { public int widgetCount; }")
        for (source in listOf(
            "<% demo.Widget widget = null; widget.widgetCou<caret> %>",
            "<%! demo.Widget widget; int value = widget.widgetCou<caret>; %>",
        )) {
            myFixture.configureByText(JspFileType.INSTANCE, source)
            val document = hostDocument()
            val variants = myFixture.completeBasic()
            assertTrue(variants?.map { it.lookupString }.toString(),
                document.text.contains("widgetCount") || variants.orEmpty().any { it.lookupString == "widgetCount" })
        }
    }

    fun testJavaSyntaxHighlightingInAllRegions() {
        val source = "<%! int count = 1; %><% boolean active = true; // comment %><%= \"한글\" %><% int next = 2; %>"
        val highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, JspFileType.INSTANCE)
        highlighter.setText(source)
        for (word in listOf("int", "boolean", "true")) {
            val offset = source.indexOf(word)
            val token = highlighter.createIterator(offset)
            assertTrue("Missing Java keyword highlighting at $word", JavaHighlightingColors.KEYWORD in token.textAttributesKeys)
        }
        assertTrue(JavaHighlightingColors.STRING in highlighter.createIterator(source.indexOf('"')).textAttributesKeys)
        assertTrue(JavaHighlightingColors.LINE_COMMENT in highlighter.createIterator(source.indexOf("//")).textAttributesKeys)
        assertTrue(JavaHighlightingColors.KEYWORD in highlighter.createIterator(source.lastIndexOf("int")).textAttributesKeys)
        assertSame(JspTokenTypes.DECLARATION, highlighter.createIterator(0).tokenType)
    }

    fun testJavaFormattingAndIdempotence() {
        val source = "<%! int sum(int a,int b){return a+b;} %>\n<div><% int count=1;if(count>0){ %>" +
            "<span><%= count+1 %></span><% } %></div>"
        myFixture.configureByText(JspFileType.INSTANCE, source)
        reformat()
        val result = myFixture.editor.document.text
        assertTrue(result, result.contains("a + b"))
        assertTrue(result, result.contains("\n    return a + b;"))
        assertTrue(result, result.contains("count = 1"))
        assertTrue(result, result.contains("count + 1"))
        assertEquals(source.filterNot(Char::isWhitespace), result.filterNot(Char::isWhitespace))
        reformat()
        assertEquals(result, myFixture.editor.document.text)
    }

    fun testSelectedJavaFormattingPreservesOutside() {
        val prefix = "<% int before=1; %><div>"
        val selected = "<% int selected=2; %>"
        val suffix = "</div><% int after=3; %>"
        myFixture.configureByText(JspFileType.INSTANCE, prefix + selected + suffix)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformatText(myFixture.file, prefix.length, prefix.length + selected.length)
        }
        val result = myFixture.editor.document.text
        assertTrue(result, result.startsWith(prefix))
        assertTrue(result, result.endsWith(suffix))
        assertTrue(result, result.contains("selected = 2"))
    }

    fun testFormattingLaterDeclarationAndLiteralContent() {
        val source = "<% int count=1; %><%! int value=2; %>" +
            "<%= \"한글  \\n\"+count %>"
        myFixture.configureByText(JspFileType.INSTANCE, source)
        reformat()
        val result = myFixture.editor.document.text
        assertTrue(result, result.contains("count = 1"))
        assertTrue(result, result.contains("value = 2"))
        assertTrue(result, result.contains("\"한글  \\n\""))
        reformat()
        assertEquals(result, myFixture.editor.document.text)
    }

    fun testJavaCodeStyleSettingsAreReused() {
        val settings = CodeStyle.getSettings(project).clone()
        settings.getCommonSettings(JavaLanguage.INSTANCE).SPACE_AROUND_ADDITIVE_OPERATORS = false
        CodeStyle.setTemporarySettings(project, settings)
        try {
            myFixture.configureByText(JspFileType.INSTANCE, "<%= 1 + 2 %>")
            reformat()
            assertEquals("<%= 1+2 %>", myFixture.editor.document.text)
        } finally {
            CodeStyle.dropTemporarySettings(project)
        }
    }

    fun testPartialHostSelectionPreservesOutside() {
        val prefix = "<% int before=1; "
        val selected = "int inside=2;"
        val suffix = " int after=3; %>"
        myFixture.configureByText(JspFileType.INSTANCE, prefix + selected + suffix)
        WriteCommandAction.runWriteCommandAction(project) {
            CodeStyleManager.getInstance(project).reformatText(myFixture.file, prefix.length, prefix.length + selected.length)
        }
        assertEquals(prefix + "int inside = 2;" + suffix, myFixture.editor.document.text)
    }

    fun testUnfinishedHostAndEscaperPreserveText() {
        for (source in listOf("<%", "<%=", "<%!", "<% int n=1;", "<%= \"한글\\n\"", "<% // trailing comment")) {
            myFixture.configureByText(JspFileType.INSTANCE, source)
            val host = myFixture.file.children.filterIsInstance<JspJavaInjectionHost>().single()
            val decoded = StringBuilder()
            assertTrue(host.createLiteralTextEscaper().decode(host.bodyRange, decoded))
            assertEquals(host.bodyRange.substring(source), decoded.toString())
            reformat()
            assertEquals(source.filterNot(Char::isWhitespace), myFixture.editor.document.text.filterNot(Char::isWhitespace))
        }
    }

    private fun injectedJava(): PsiJavaFile {
        val host = myFixture.file.children.filterIsInstance<JspJavaInjectionHost>().last()
        return InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(host)!!.first().first as PsiJavaFile
    }

    private fun hostDocument() = PsiDocumentManager.getInstance(project).getDocument(
        InjectedLanguageManager.getInstance(project).getTopLevelFile(myFixture.file))!!

    private fun reformat() {
        WriteCommandAction.runWriteCommandAction(project) { CodeStyleManager.getInstance(project).reformat(myFixture.file) }
        PsiDocumentManager.getInstance(project).commitAllDocuments()
    }
}
