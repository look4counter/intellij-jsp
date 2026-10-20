package com.look4counter.jsp

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementDecorator
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiNewExpression
import com.intellij.psi.util.PsiTreeUtil

class JspJavaImportCompletionContributor : CompletionContributor() {
    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        if (JspJavaImports.host(parameters.position) == null ||
            JspJavaImports.reference(parameters.position) == null) return
        result.runRemainingContributors(parameters) { completion ->
            val item = completion.lookupElement
            val target = item.psiElement as? PsiClass ?: item.`object` as? PsiClass
            if (target?.qualifiedName == null) {
                result.passResult(completion)
            } else {
                result.passResult(completion.withLookupElement(object : LookupElementDecorator<LookupElement>(item) {
                    override fun handleInsert(context: InsertionContext) {
                        context.commitDocument()
                        val file = JspJavaImports.host(context.file) ?: return
                        val position = context.file.findElementAt(context.startOffset) ?: return
                        val decision = JspJavaImports.decide(file, position, target)
                        context.document.replaceString(context.startOffset, context.tailOffset, decision.text)
                        context.tailOffset = context.startOffset + decision.text.length
                        context.editor.caretModel.moveToOffset(context.tailOffset)
                        context.commitDocument()
                        val inserted = context.file.findElementAt(context.startOffset)
                        if (PsiTreeUtil.getParentOfType(inserted, PsiNewExpression::class.java) != null) {
                            ConstructorInsertHandler.insertParentheses(context, this, target, false)
                        }
                        if (decision.addImport) JspJavaImports.addImport(file, target.qualifiedName!!)
                    }
                }))
            }
        }
    }
}
