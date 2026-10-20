package com.look4counter.jsp

import com.intellij.codeInsight.daemon.QuickFixActionRegistrar
import com.intellij.codeInsight.daemon.impl.quickfix.ImportClassFix
import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.*

class JspJavaImportQuickFixProvider : UnresolvedReferenceQuickFixProvider<PsiJavaCodeReferenceElement>() {
    override fun getReferenceClass() = PsiJavaCodeReferenceElement::class.java

    override fun registerFixes(ref: PsiJavaCodeReferenceElement, registrar: QuickFixActionRegistrar) {
        if (JspJavaImports.reference(ref) == null || ref.resolve() != null) return
        for (target in ImportClassFix(ref).getClassesToImport(true)) {
            val name = target.qualifiedName ?: continue
            registrar.register(ImportAction(SmartPointerManager.createPointer(ref), name))
        }
    }

    private class ImportAction(
        val pointer: SmartPsiElementPointer<PsiJavaCodeReferenceElement>, val qualifiedName: String,
    ) : IntentionAction {
        override fun getText() = "JSP import 추가: $qualifiedName"
        override fun getFamilyName() = "JSP import 추가"
        override fun startInWriteAction() = true
        override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
            val ref = pointer.element ?: return false
            return JspJavaImports.reference(ref) != null && ref.resolve() == null && candidate(ref) != null
        }
        private fun candidate(ref: PsiJavaCodeReferenceElement): PsiClass? {
            val facade = JavaPsiFacade.getInstance(ref.project)
            val target = facade.findClass(qualifiedName, ref.resolveScope) ?: return null
            return target.takeIf { it.name == ref.referenceName && facade.resolveHelper.isAccessible(it, ref, null) }
        }

        override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
            val ref = pointer.element ?: return
            val host = JspJavaImports.host(ref) ?: return
            if (ref.resolve() != null || JspJavaImports.reference(ref) == null) return
            val target = candidate(ref) ?: return
            val decision = JspJavaImports.decide(host, ref, target)
            val manager = InjectedLanguageManager.getInstance(project)
            val nameElement = ref.referenceNameElement ?: return
            val range = manager.injectedToHost(ref, nameElement.textRange)
            val document = PsiDocumentManager.getInstance(project).getDocument(host) ?: return
            document.replaceString(range.startOffset, range.endOffset, decision.text)
            if (decision.addImport) JspJavaImports.addImport(host, qualifiedName)
        }
    }
}
