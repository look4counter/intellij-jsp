package com.look4counter.jsp

import com.intellij.icons.AllIcons
import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

class JspFileType private constructor() : LanguageFileType(JspLanguage) {
    override fun getName(): String = "Community JSP"

    override fun getDescription(): String = "JSP"

    override fun getDefaultExtension(): String = "jsp"

    override fun getIcon(): Icon = AllIcons.FileTypes.Jsp

    companion object {
        @JvmField
        val INSTANCE = JspFileType()
    }
}
