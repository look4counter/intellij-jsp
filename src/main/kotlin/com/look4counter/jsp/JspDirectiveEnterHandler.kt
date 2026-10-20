package com.look4counter.jsp

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorActionHandler
import com.intellij.openapi.util.Ref
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

class JspDirectiveEnterHandler : EnterHandlerDelegate {
    override fun preprocessEnter(
        file: PsiFile, editor: Editor, caretOffset: Ref<Int>, caretAdvance: Ref<Int>,
        dataContext: DataContext, originalHandler: EditorActionHandler?,
    ): EnterHandlerDelegate.Result {
        if (file.viewProvider.baseLanguage != JspLanguage ||
            InjectedLanguageManager.getInstance(file.project).isInjectedFragment(file) ||
            editor.selectionModel.hasSelection() || editor.caretModel.caretCount != 1) return EnterHandlerDelegate.Result.Continue
        val document = editor.document
        val offset = caretOffset.get()
        if (offset <= 0 || offset > document.textLength || !document.isWritable) return EnterHandlerDelegate.Result.Continue
        val text = document.charsSequence
        val lineStart = document.getLineStartOffset(document.getLineNumber(offset))
        var end = offset
        while (end > lineStart && (text[end - 1] == ' ' || text[end - 1] == '\t')) end--
        if (end < 2 || text[end - 2] != '%' || text[end - 1] != '>') return EnterHandlerDelegate.Result.Continue
        val lineEnd = document.getLineEndOffset(document.getLineNumber(offset))
        if (text.subSequence(offset, lineEnd).any { it != ' ' && it != '\t' }) return EnterHandlerDelegate.Result.Continue
        PsiDocumentManager.getInstance(file.project).commitDocument(document)
        val jsp = file.viewProvider.getPsi(JspLanguage) ?: return EnterHandlerDelegate.Result.Continue
        val leaf = jsp.findElementAt(end - 1) ?: return EnterHandlerDelegate.Result.Continue
        val directive = PsiTreeUtil.getParentOfType(leaf, JspDirective::class.java, false) ?: return EnterHandlerDelegate.Result.Continue
        if (directive.textRange.endOffset != end ||
            JspDirectiveSyntax.parse(directive.text, requireClosed = true)?.name !in DIRECTIVES) return EnterHandlerDelegate.Result.Continue
        val start = directive.textRange.startOffset
        val startLine = document.getLineStartOffset(document.getLineNumber(start))
        val indent = text.subSequence(startLine, start).toString()
        if (indent.any { it != ' ' && it != '\t' }) return EnterHandlerDelegate.Result.Continue
        val insertion = "\n$indent"
        document.insertString(offset, insertion)
        editor.caretModel.moveToOffset(offset + insertion.length)
        caretOffset.set(offset + insertion.length)
        return EnterHandlerDelegate.Result.Stop
    }

    companion object {
        private val DIRECTIVES = setOf("page", "include", "taglib")
    }
}
