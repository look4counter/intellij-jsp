package com.look4counter.jsp

import com.intellij.ide.highlighter.HtmlFileHighlighter
import com.intellij.openapi.editor.colors.EditorColorsScheme
import com.intellij.openapi.editor.ex.util.LayerDescriptor
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter
import com.intellij.openapi.editor.highlighter.EditorHighlighter
import com.intellij.openapi.fileTypes.EditorHighlighterProvider
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class JspEditorHighlighterProvider : EditorHighlighterProvider {
    override fun getEditorHighlighter(
        project: Project?,
        fileType: FileType,
        virtualFile: VirtualFile?,
        colors: EditorColorsScheme,
    ): EditorHighlighter = LayeredLexerEditorHighlighter(JspSyntaxHighlighter(), colors).apply {
        // Keep host fragments in one HTML document, including embedded JS/CSS when available.
        registerLayer(JspTokenTypes.HTML_TEXT, LayerDescriptor(HtmlFileHighlighter(), ""))
        for (type in JspParserDefinition.JAVA_TOKENS) {
            registerLayer(type, LayerDescriptor(JspJavaSyntaxHighlighter(type), "\n"))
        }
    }
}
