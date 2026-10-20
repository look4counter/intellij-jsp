package com.look4counter.jsp

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.JavaPsiFacade
import com.intellij.openapi.project.DumbService

object JspDirectiveSyntax {
    data class Attribute(val name: String, val value: String, val valueRange: TextRange, val quote: Char)
    data class Parsed(
        val name: String, val nameRange: TextRange, val attributes: Map<String, Attribute>,
        val attributeList: List<Attribute> = attributes.values.toList(),
    )
    private val NAME = Regex("^<%@\\s*([A-Za-z]+)\\b")
    private val ATTRIBUTE = Regex("([\\w:.-]+)\\s*=\\s*(['\"])(.*?)\\2", RegexOption.DOT_MATCHES_ALL)

    fun parse(text: String, requireClosed: Boolean = false): Parsed? {
        if (requireClosed && !text.endsWith("%>")) return null
        val name = NAME.find(text) ?: return null
        val begin = name.range.last + 1
        val end = if (text.endsWith("%>")) text.length - 2 else text.length
        val attributes = ATTRIBUTE.findAll(text.substring(begin, end)).map { match ->
            val value = match.groups[3]!!
            Attribute(match.groupValues[1], value.value,
                TextRange(value.range.first + begin, value.range.last + begin + 1), match.groupValues[2][0])
        }.toList()
        val group = name.groups[1]!!
        return Parsed(group.value, TextRange(group.range.first, group.range.last + 1), attributes.associateBy { it.name }, attributes)
    }
}

class JspDirective(node: ASTNode) : ASTWrapperPsiElement(node) {
    override fun getReferences(): Array<PsiReference> {
        val directive = JspDirectiveSyntax.parse(text) ?: return PsiReference.EMPTY_ARRAY
        if (directive.name == "page") return importReferences(directive)
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

    private fun importReferences(directive: JspDirectiveSyntax.Parsed): Array<PsiReference> {
        if (!text.endsWith("%>")) return PsiReference.EMPTY_ARRAY
        val references = mutableListOf<PsiReference>()
        for (attribute in directive.attributeList.filter { it.name == "import" }) {
            for (entry in IMPORT_ENTRY.findAll(attribute.value)) {
                val name = entry.value.trim()
                if (!CLASS_NAME.matches(name)) continue
                val start = attribute.valueRange.startOffset + entry.range.first + entry.value.indexOfFirst { !it.isWhitespace() }
                references += object : PsiReferenceBase<JspDirective>(this, TextRange.from(start, name.length), true) {
                    override fun resolve(): PsiElement? =
                        if (DumbService.isDumb(project)) null else JavaPsiFacade.getInstance(project).findClass(name, resolveScope)

                    override fun getVariants(): Array<Any> = emptyArray()
                }
            }
        }
        return references.toTypedArray()
    }

    companion object {
        private val IMPORT_ENTRY = Regex("[^,]+")
        private val CLASS_NAME = Regex("[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*(?:\\.[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*)+")
    }
}
