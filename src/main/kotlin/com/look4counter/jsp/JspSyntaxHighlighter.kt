package com.look4counter.jsp

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class JspSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer() = JspHighlightingLexer()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> = when (tokenType) {
        JspTokenTypes.DIRECTIVE -> pack(DIRECTIVE)
        JspTokenTypes.SCRIPTLET -> pack(SCRIPTLET)
        JspTokenTypes.EXPRESSION -> pack(EXPRESSION)
        JspTokenTypes.DECLARATION -> pack(DECLARATION)
        JspTokenTypes.COMMENT -> pack(COMMENT)
        JspTokenTypes.EL -> pack(EL)
        else -> emptyArray()
    }

    companion object {
        val DIRECTIVE = TextAttributesKey.createTextAttributesKey("COMMUNITY_JSP_DIRECTIVE", DefaultLanguageHighlighterColors.METADATA)
        val SCRIPTLET = TextAttributesKey.createTextAttributesKey("COMMUNITY_JSP_SCRIPTLET", DefaultLanguageHighlighterColors.KEYWORD)
        val EXPRESSION = TextAttributesKey.createTextAttributesKey("COMMUNITY_JSP_EXPRESSION", DefaultLanguageHighlighterColors.KEYWORD)
        val DECLARATION = TextAttributesKey.createTextAttributesKey("COMMUNITY_JSP_DECLARATION", DefaultLanguageHighlighterColors.KEYWORD)
        val COMMENT = TextAttributesKey.createTextAttributesKey("COMMUNITY_JSP_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val EL = TextAttributesKey.createTextAttributesKey("COMMUNITY_JSP_EL", DefaultLanguageHighlighterColors.CONSTANT)
    }
}
