package com.look4counter.jsp

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase

object JspDirectiveSyntax {
    data class Attribute(val name: String, val value: String, val valueRange: TextRange, val quote: Char)
    data class Parsed(val name: String, val nameRange: TextRange, val attributes: Map<String, Attribute>)
    private val NAME = Regex("^<%@\\s*([A-Za-z]+)\\b")
    private val ATTRIBUTE = Regex("([\\w:.-]+)\\s*=\\s*(['\"])(.*?)\\2", RegexOption.DOT_MATCHES_ALL)

    fun parse(text: String, requireClosed: Boolean = false): Parsed? {
        if (requireClosed && !text.endsWith("%>")) return null
        val name = NAME.find(text) ?: return null
        val begin = name.range.last + 1
        val end = if (text.endsWith("%>")) text.length - 2 else text.length
        val attributes = ATTRIBUTE.findAll(text.substring(begin, end)).associate { match ->
            val value = match.groups[3]!!
            match.groupValues[1] to Attribute(match.groupValues[1], value.value,
                TextRange(value.range.first + begin, value.range.last + begin + 1), match.groupValues[2][0])
        }
        val group = name.groups[1]!!
        return Parsed(group.value, TextRange(group.range.first, group.range.last + 1), attributes)
    }
}

class JspDirective(node: ASTNode) : ASTWrapperPsiElement(node) {
    override fun getReferences(): Array<PsiReference> {
        val directive = JspDirectiveSyntax.parse(text) ?: return PsiReference.EMPTY_ARRAY
        if (directive.name != "include") return PsiReference.EMPTY_ARRAY
        val attribute = directive.attributes["file"] ?: return PsiReference.EMPTY_ARRAY
        return arrayOf(object : PsiReferenceBase<JspDirective>(this, attribute.valueRange, true) {
            override fun resolve(): PsiElement? {
                val source = containingFile.originalFile.virtualFile ?: return null
                val target = JspIncludeContext.resolveInclude(source, attribute.value, project) ?: return null
                return PsiManager.getInstance(project).findFile(target)
            }
            override fun getVariants(): Array<Any> = emptyArray()
        })
    }

    override fun getReference(): PsiReference? = references.firstOrNull()
}
