package com.look4counter.jsp

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.impl.FakePsiElement
import com.intellij.psi.util.PsiTreeUtil

class JspGotoDeclarationHandler : GotoDeclarationHandler {
    override fun getGotoDeclarationTargets(sourceElement: PsiElement?, offset: Int, editor: Editor): Array<PsiElement>? {
        val source = sourceElement ?: return null
        if (DumbService.isDumb(source.project)) return null
        val manager = InjectedLanguageManager.getInstance(source.project)
        val topLevelFile = manager.getTopLevelFile(source)
        if (topLevelFile !is JspPsiFile) return null
        val element = if (source.language == JavaLanguage.INSTANCE) source else {
            manager.findInjectedElementAt(topLevelFile, offset) ?: return null
        }
        val reference = PsiTreeUtil.getParentOfType(element, PsiReferenceExpression::class.java, false) ?: return null
        val method = reference.resolve() as? PsiMethod ?: return null
        val identifier = method.nameIdentifier ?: return null
        val spans = method.containingFile.getUserData(JspJavaSourceMap.KEY)
        if (spans == null) {
            return if (!manager.isInjectedFragment(method.containingFile)) arrayOf(method.navigationElement) else null
        }
        val span = spans.firstOrNull { it.generated.contains(identifier.textRange) } ?: return null
        val targetFile = PsiManager.getInstance(source.project).findFile(span.file) ?: return null
        val targetOffset = span.sourceStart + identifier.textOffset - span.generated.startOffset
        if (targetOffset < 0 || targetOffset + identifier.textLength > targetFile.textLength) return null
        if (targetFile.text.substring(targetOffset, targetOffset + identifier.textLength) != identifier.text) return null
        return arrayOf(JspSourceDeclaration(targetFile, targetOffset, identifier.text))
    }
}

/** A JSP declaration leaf contains the entire block; retain the exact name offset. */
private class JspSourceDeclaration(private val file: PsiFile, private val offset: Int, private val identifier: String) : FakePsiElement() {
    override fun getParent(): PsiElement = file
    override fun getContainingFile(): PsiFile = file
    override fun getNavigationElement(): PsiElement = this
    override fun getName() = identifier
    override fun getText() = identifier
    override fun getTextOffset() = offset
    override fun getTextRange() = TextRange.from(offset, identifier.length)
    override fun isValid() = file.isValid && offset + identifier.length <= file.textLength
    override fun canNavigate() = isValid && file.virtualFile != null
    override fun canNavigateToSource() = canNavigate()
    override fun navigate(requestFocus: Boolean) {
        if (canNavigate()) OpenFileDescriptor(project, file.virtualFile, offset).navigate(requestFocus)
    }
}
