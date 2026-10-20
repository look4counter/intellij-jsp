package com.look4counter.jsp

import com.intellij.formatting.Alignment
import com.intellij.formatting.ASTBlock
import com.intellij.formatting.FormattingContext
import com.intellij.formatting.FormattingModel
import com.intellij.formatting.Wrap
import com.intellij.formatting.Indent
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock
import com.intellij.formatting.templateLanguages.TemplateLanguageBlockFactory
import com.intellij.formatting.templateLanguages.TemplateLanguageFormattingModelBuilder
import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlTag
import com.intellij.ide.highlighter.HtmlFileType

class JspFormattingModelBuilder : TemplateLanguageFormattingModelBuilder() {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val delegate = super.createModel(formattingContext)
        return object : FormattingModel by delegate {
            override fun shiftIndentInsideRange(node: ASTNode?, range: TextRange, indent: Int): TextRange {
                // The default document model also shifts whitespace inside multiline leaves.
                // JSP leaves are opaque: only their surrounding HTML whitespace may change.
                if (node != null && node.firstChildNode == null && node.elementType.language == JspLanguage &&
                    node.elementType != JspTokenTypes.HTML_TEXT
                ) return range
                return delegate.shiftIndentInsideRange(node, range, indent)
            }
        }
    }

    override fun createTemplateLanguageBlock(
        node: ASTNode,
        wrap: Wrap?,
        alignment: Alignment?,
        foreignChildren: List<DataLanguageBlockWrapper>?,
        settings: CodeStyleSettings,
    ): TemplateLanguageBlock = JspFormattingBlock(node, wrap, alignment, this, settings, foreignChildren)
}

private class JspFormattingBlock(
    node: ASTNode,
    wrap: Wrap?,
    alignment: Alignment?,
    factory: TemplateLanguageBlockFactory,
    settings: CodeStyleSettings,
    foreignChildren: List<DataLanguageBlockWrapper>?,
) : TemplateLanguageBlock(node, wrap, alignment, factory, settings, foreignChildren) {
    // Server constructs remain single leaves; the platform merges the existing HTML blocks around them.
    override fun getTemplateTextElementType() = JspTokenTypes.HTML_TEXT

    override fun getIndent(): Indent? {
        if (node.elementType == JspTokenTypes.DIRECTIVE || node.elementType == JspTokenTypes.DECLARATION)
            return Indent.getNoneIndent()
        val declaration = node.elementType == JspParserDefinition.JAVA_HOST &&
            node.firstChildNode?.elementType == JspTokenTypes.DECLARATION
        if (node.elementType != JspParserDefinition.DIRECTIVE_HOST && !declaration) return super.getIndent()
        val htmlParent = (parent as? DataLanguageBlockWrapper)?.original ?: return Indent.getNoneIndent()
        val htmlPsi = (htmlParent as? ASTBlock)?.node?.psi
        if (htmlPsi != null && PsiTreeUtil.getParentOfType(htmlPsi, XmlTag::class.java, false) == null)
            return Indent.getNoneIndent()
        val children = htmlParent.subBlocks
        val next = children.indexOfFirst { it.textRange.startOffset >= textRange.startOffset }
        val index = if (next < 0) children.size else next
        val indent = htmlParent.getChildAttributes(index).childIndent ?: return Indent.getNoneIndent()
        return if (indent.type == Indent.Type.NORMAL)
            Indent.getSpaceIndent(settings.getIndentOptions(HtmlFileType.INSTANCE).INDENT_SIZE) else indent
    }
}
