package com.look4counter.jsp

import com.intellij.codeInsight.AutoPopupController
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class JspDirectiveAutoPopupHandler : TypedHandlerDelegate() {
    override fun checkAutoPopup(charTyped: Char, project: Project, editor: Editor, file: PsiFile): Result {
        if (charTyped !in "@=\"'.,/" || file.viewProvider.baseLanguage != JspLanguage) return Result.CONTINUE
        val text = editor.document.charsSequence
        val offset = editor.caretModel.offset
        val lexer = JspHighlightingLexer()
        lexer.start(text)
        while (lexer.tokenType != null && lexer.tokenEnd < offset) lexer.advance()
        val directive = lexer.tokenType == JspTokenTypes.DIRECTIVE && offset >= lexer.tokenStart + 3 &&
            !text.subSequence(lexer.tokenStart, offset).trimEnd().endsWith("%>")
        val opening = charTyped == '@' && lexer.tokenType == JspTokenTypes.SCRIPTLET &&
            lexer.tokenStart == offset - 2 && text.subSequence(lexer.tokenStart, offset).toString() == "<%"
        if (!directive && !opening) return Result.CONTINUE
        AutoPopupController.getInstance(project).scheduleAutoPopup(editor)
        return Result.STOP
    }
}
