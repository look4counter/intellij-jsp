package com.look4counter.jsp

import com.intellij.lang.Language
import com.intellij.lang.LanguageParserDefinitions
import com.intellij.lang.html.HTMLLanguage
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.FileViewProvider
import com.intellij.psi.FileViewProviderFactory
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.source.PsiFileImpl
import com.intellij.psi.templateLanguages.TemplateDataElementType
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.OuterLanguageElementType

class JspFileViewProvider(manager: PsiManager, file: VirtualFile, physical: Boolean) :
    MultiplePsiFilesPerDocumentFileViewProvider(manager, file, physical), TemplateLanguageFileViewProvider {

    override fun getBaseLanguage(): Language = JspLanguage
    override fun getTemplateDataLanguage(): Language = HTMLLanguage.INSTANCE
    override fun getLanguages(): Set<Language> = setOf(JspLanguage, HTMLLanguage.INSTANCE)

    override fun getContentElementType(language: Language): IElementType? =
        if (language == HTMLLanguage.INSTANCE) HTML_DATA else null

    override fun createFile(language: Language): PsiFile? {
        if (language !in languages) return null
        val definition = LanguageParserDefinitions.INSTANCE.forLanguage(language) ?: return null
        val psi = definition.createFile(this)
        if (language == HTMLLanguage.INSTANCE) {
            // Keep JSP ranges in the HTML tree so completion edits use original document offsets.
            (psi as PsiFileImpl).setContentElementType(HTML_DATA)
        }
        return psi
    }

    override fun cloneInner(fileCopy: VirtualFile) = JspFileViewProvider(manager, fileCopy, false)

    companion object {
        private val JSP_OUTER = OuterLanguageElementType("JSP_OUTER", JspLanguage)
        private val HTML_DATA = TemplateDataElementType("JSP_HTML_DATA", JspLanguage, JspTokenTypes.HTML_TEXT, JSP_OUTER)
    }
}

class JspFileViewProviderFactory : FileViewProviderFactory {
    override fun createFileViewProvider(
        file: VirtualFile,
        language: Language,
        manager: PsiManager,
        eventSystemEnabled: Boolean,
    ): FileViewProvider = JspFileViewProvider(manager, file, eventSystemEnabled)
}
