package com.look4counter.jsp

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.project.DumbService
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil

internal object JspJavaImports {
    data class Decision(val text: String, val addImport: Boolean)

    fun host(element: PsiElement): PsiFile? {
        val file = InjectedLanguageManager.getInstance(element.project).getTopLevelFile(element)
        return file.takeIf { it.fileType == JspFileType.INSTANCE && it.isWritable && !DumbService.isDumb(it.project) }
    }

    fun reference(element: PsiElement): PsiJavaCodeReferenceElement? {
        if (PsiTreeUtil.getParentOfType(element, PsiComment::class.java, false) != null ||
            PsiTreeUtil.getParentOfType(element, PsiLiteralExpression::class.java, false) != null) return null
        val ref = PsiTreeUtil.getParentOfType(element, PsiJavaCodeReferenceElement::class.java, false) ?: return null
        if (host(ref) == null || ref.qualifier != null) return null
        val manager = InjectedLanguageManager.getInstance(ref.project)
        val nameRange = ref.referenceNameElement?.textRange ?: return null
        if (manager.intersectWithAllEditableFragments(ref.containingFile, nameRange).none { it == nameRange }) return null
        val offset = manager.injectedToHost(ref, ref.textOffset)
        val file = host(ref) ?: return null
        return ref.takeIf { file.findElementAt(offset)?.parent is JspJavaInjectionHost ||
            file.findElementAt(offset) is JspJavaInjectionHost }
    }

    fun decide(file: PsiFile, context: PsiElement, target: PsiClass): Decision {
        val qualified = target.qualifiedName ?: return Decision(target.name.orEmpty(), false)
        val name = target.name ?: return Decision(qualified, false)
        val facade = JavaPsiFacade.getInstance(file.project)
        val resolved = facade.elementFactory.createReferenceFromText(name, context).resolve()
        if (resolved is PsiClass && file.manager.areElementsEquivalent(resolved, target)) return Decision(name, false)
        if (resolved != null) return Decision(qualified, false)
        val imports = JspIncludeContext.build(file).imports + "java.lang.*"
        if (imports.any { !it.endsWith(".*") && it.substringAfterLast('.') == name && it != qualified }) {
            return Decision(qualified, false)
        }
        // A new explicit import must not shadow types already supplied by wildcard imports.
        for (entry in imports.filter { it.endsWith(".*") }) {
            val other = facade.findClass(entry.removeSuffix("*") + name, context.resolveScope)
            if (other != null && !file.manager.areElementsEquivalent(other, target)) return Decision(qualified, false)
        }
        return Decision(name, true)
    }

    fun addImport(file: PsiFile, qualifiedName: String) {
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return
        val offset = if (document.text.startsWith('\uFEFF')) 1 else 0
        document.insertString(offset, "<%@ page import=\"$qualifiedName\" %>\n")
    }
}
