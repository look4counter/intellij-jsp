package com.look4counter.jsp

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiLanguageInjectionHost
import com.intellij.psi.impl.source.tree.injected.InjectionBackgroundSuppressor

open class JspJavaInjectionHost(node: ASTNode) : ASTWrapperPsiElement(node), PsiLanguageInjectionHost {
    val kind get() = node.firstChildNode.elementType
    val bodyRange: TextRange
        get() {
            val start = if (kind == JspTokenTypes.SCRIPTLET) 2 else 3
            val end = if (text.endsWith("%>")) textLength - 2 else textLength
            return TextRange(start.coerceAtMost(end), end)
        }

    override fun isValidHost() = true
    override fun createLiteralTextEscaper(): LiteralTextEscaper<out PsiLanguageInjectionHost> =
        LiteralTextEscaper.createSimple(this, false)

    override fun updateText(text: String): PsiLanguageInjectionHost {
        val file = PsiFileFactory.getInstance(project).createFileFromText("fragment.jsp", JspFileType.INSTANCE, text)
        val replacement = file.children.filterIsInstance<JspJavaInjectionHost>().first()
        return replace(replacement) as PsiLanguageInjectionHost
    }
}

class JspDeclarationInjectionHost(node: ASTNode) : JspJavaInjectionHost(node), InjectionBackgroundSuppressor
