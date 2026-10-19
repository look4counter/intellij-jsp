package com.look4counter.jsp

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.html.embedding.HtmlEmbeddedContentSupport
import com.intellij.lang.HtmlScriptContentProvider
import com.intellij.lang.Language
import com.intellij.lang.LanguageHtmlScriptContentProvider
import com.intellij.lexer.EmbeddedTokenTypesProvider
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.ExtensionTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.util.KeyedLazyInstance

class JspEditorHighlighterTest : BasePlatformTestCase() {
    fun testRegisteredProviderAndJspColors() {
        assertSame(JspFileType.INSTANCE, FileTypeManager.getInstance().getFileTypeByFileName("sample.jsp"))
        val source = "<h1><%= title %></h1>"
        val highlighter = jspHighlighter(source)
        val iterator = highlighter.createIterator(source.indexOf("<%="))
        assertSame(JspTokenTypes.EXPRESSION, iterator.tokenType)
        assertEquals(listOf(JspSyntaxHighlighter.EXPRESSION), iterator.textAttributesKeys.toList())
        assertEquals(source.length, coveredLength(highlighter))
    }

    fun testHtmlTagAttributeAndCommentHighlightingMatchesHtml() {
        assertHostMatches(
            "<!-- before --><div class=\"prefix",
            "\${value}",
            "suffix\" data-x='한글'>content &amp;</div>",
        )
        assertHostMatches("<p>", "<%-- <script>not JavaScript</script><style> --%>", "after</p>")
    }

    fun testScriptAndStyleReuseAvailableLanguageHighlighters() {
        val script = "<script>const label = \"before"
        val scriptTail = "after\"; console.log(label, 42);</script>"
        val scriptHighlighter = assertHostMatches(script, "<%= name %>", scriptTail)
        val style = "<style>.card { color: red; content: \"before"
        val styleTail = "after\"; margin: 12px; }</style>"
        val styleHighlighter = assertHostMatches(style, "\${color}", styleTail)

        if (java.lang.Boolean.getBoolean("jsp.test.web.languages")) {
            assertEquals("JavaScript", scriptHighlighter.createIterator(script.indexOf("const")).tokenType.language.id)
            assertEquals("CSS", styleHighlighter.createIterator(style.indexOf("color")).tokenType.language.id)
        }
    }

    fun testHtmlAndJspWithoutEmbeddedLanguageProviders() {
        val disposable = Disposer.newDisposable()
        try {
            ExtensionTestUtil.maskExtensions(HtmlEmbeddedContentSupport.EP_NAME, emptyList(), disposable, true)
            ExtensionTestUtil.maskExtensions(EmbeddedTokenTypesProvider.EXTENSION_POINT_NAME, emptyList(), disposable, true)
            val scriptProviders = ExtensionPointName.create<KeyedLazyInstance<HtmlScriptContentProvider>>(
                "com.intellij.html.scriptContentProvider",
            )
            ExtensionTestUtil.maskExtensions(scriptProviders, emptyList(), disposable, true)
            clearScriptProviderCache()
            val before = "<script>const value = \"before"
            val after = "after\";</script><style>.card { color: red; }</style><h1>한글</h1>"
            val highlighter = assertHostMatches(before, "<%= name %>", after)
            assertSame(JspTokenTypes.EXPRESSION, highlighter.createIterator(before.length).tokenType)
            val iterator = highlighter.createIterator(0)
            while (!iterator.atEnd()) {
                assertFalse(iterator.tokenType.language.id in setOf("JavaScript", "CSS"))
                iterator.advance()
            }
        } finally {
            Disposer.dispose(disposable)
            clearScriptProviderCache()
        }
    }

    private fun clearScriptProviderCache() {
        Language.findLanguageByID("JavaScript")?.let { LanguageHtmlScriptContentProvider.INSTANCE.clearCache(it) }
    }

    fun testIncrementalDelimiterEditsMatchFreshHighlighting() {
        val source = "<div title=\"before\${value}after\"><%-- <script> --%>한글</div>"
        myFixture.configureByText(JspFileType.INSTANCE, source)
        val editor = myFixture.editor as EditorEx
        val document = editor.document

        fun checkFresh() {
            val text = document.text
            val fresh = jspHighlighter(text)
            assertEquals(text.length, coveredLength(editor.highlighter))
            for (offset in text.indices) {
                val expected = fresh.createIterator(offset)
                val actual = editor.highlighter.createIterator(offset)
                assertEquals("Token at $offset in $text", expected.tokenType, actual.tokenType)
                assertEquals("Attributes at $offset", expected.textAttributes, actual.textAttributes)
            }
        }

        fun replace(start: Int, end: Int, value: String) {
            WriteCommandAction.runWriteCommandAction(project) { document.replaceString(start, end, value) }
            checkFresh()
        }

        checkFresh()
        var end = document.text.indexOf("--%>")
        replace(end, end + 4, "")
        replace(end, end, "--%>")
        val elEnd = document.text.indexOf('}')
        replace(elEnd, elEnd + 1, "")
        replace(elEnd, elEnd, "}")
        val elStart = document.text.indexOf("\${")
        replace(elStart, elStart + 2, "")
        replace(elStart, elStart, "\${")
        end = document.text.indexOf("</div>")
        replace(end, end, "<%= title %>")
        assertEquals(source.replace("</div>", "<%= title %></div>"), document.text)
    }

    fun testThemeChangesKeepHtmlAndJspAttributesInSync() {
        val source = "<b><%@ page language=\"java\" %>\${name}</b>"
        val highlighter = jspHighlighter(source)
        for (scheme in EditorColorsManager.getInstance().allSchemes) {
            highlighter.setColorScheme(scheme)
            val fresh = JspEditorHighlighterProvider().getEditorHighlighter(project, JspFileType.INSTANCE, null, scheme)
            fresh.setText(source)
            for (offset in source.indices) {
                assertEquals(fresh.createIterator(offset).textAttributes, highlighter.createIterator(offset).textAttributes)
            }
        }
    }

    fun testEmptyAndIncompleteFilesAreFullyCovered() {
        for (text in listOf("", "<%", "<%@ page", "<%-- unfinished", "<a title=\"\${value", "<style>.x { color: \${value")) {
            assertEquals(text.length, coveredLength(jspHighlighter(text)))
        }
    }

    private fun jspHighlighter(text: String): EditorHighlighter =
        EditorHighlighterFactory.getInstance().createEditorHighlighter(project, JspFileType.INSTANCE).apply { setText(text) }

    private fun assertHostMatches(before: String, jsp: String, after: String): EditorHighlighter {
        val source = before + jsp + after
        val highlighter = jspHighlighter(source)
        val html = LexerEditorHighlighter(HtmlFileHighlighter(), EditorColorsManager.getInstance().globalScheme)
        html.setText(before + after)
        assertEquals(source.length, coveredLength(highlighter))
        for (offset in (before + after).indices) {
            val sourceOffset = if (offset < before.length) offset else offset + jsp.length
            val expected = html.createIterator(offset)
            val actual = highlighter.createIterator(sourceOffset)
            assertEquals("Host token at $sourceOffset", expected.tokenType, actual.tokenType)
            assertEquals("Host color at $sourceOffset", expected.textAttributes, actual.textAttributes)
        }
        return highlighter
    }

    private fun coveredLength(highlighter: EditorHighlighter): Int {
        val iterator = highlighter.createIterator(0)
        var end = 0
        while (!iterator.atEnd()) {
            assertEquals("Highlighting must have no gaps", end, iterator.start)
            assertTrue(iterator.end > iterator.start)
            end = iterator.end
            iterator.advance()
        }
        return end
    }
}
