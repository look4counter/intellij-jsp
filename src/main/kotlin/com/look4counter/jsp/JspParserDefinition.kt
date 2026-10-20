package com.look4counter.jsp

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.TokenSet

class JspParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?) = JspHighlightingLexer()

    override fun createParser(project: Project?) = PsiParser { root, builder ->
        val marker = builder.mark()
        while (!builder.eof()) {
            if (builder.tokenType in JAVA_TOKENS) {
                val host = builder.mark()
                builder.advanceLexer()
                host.done(JAVA_HOST)
            } else if (builder.tokenType == JspTokenTypes.DIRECTIVE) {
                val directive = builder.mark()
                builder.advanceLexer()
                directive.done(DIRECTIVE_HOST)
            } else builder.advanceLexer()
        }
        marker.done(root)
        builder.treeBuilt
    }

    override fun getFileNodeType(): IFileElementType = FILE
    override fun getWhitespaceTokens(): TokenSet = TokenSet.EMPTY
    override fun getCommentTokens(): TokenSet = TokenSet.create(JspTokenTypes.COMMENT)
    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY
    override fun createElement(node: ASTNode): PsiElement =
        when (node.elementType) {
            JAVA_HOST -> if (node.firstChildNode?.elementType == JspTokenTypes.DECLARATION)
                JspDeclarationInjectionHost(node) else JspJavaInjectionHost(node)
            DIRECTIVE_HOST -> JspDirective(node)
            else -> ASTWrapperPsiElement(node)
        }
    override fun createFile(viewProvider: FileViewProvider): PsiFile = JspPsiFile(viewProvider)

    companion object {
        private val FILE = IFileElementType(JspLanguage)
        val JAVA_HOST = IElementType("JSP_JAVA_HOST", JspLanguage)
        val DIRECTIVE_HOST = IElementType("JSP_DIRECTIVE_HOST", JspLanguage)
        val JAVA_TOKENS = setOf(JspTokenTypes.SCRIPTLET, JspTokenTypes.EXPRESSION, JspTokenTypes.DECLARATION)
    }
}

class JspPsiFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, JspLanguage) {
    override fun getFileType() = JspFileType.INSTANCE
}
