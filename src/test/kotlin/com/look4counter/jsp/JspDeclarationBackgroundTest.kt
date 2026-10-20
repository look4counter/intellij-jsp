package com.look4counter.jsp

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JspDeclarationBackgroundTest : BasePlatformTestCase() {
    fun testDeclarationBackgroundWithoutDisablingJavaInjection() {
        val source = "<%! String title() { return \"hello\"; } %><%! int count; %>" +
            "<% int local = 1; %><%= title() %>"
        myFixture.configureByText(JspFileType.INSTANCE, source)
        val hosts = myFixture.file.children.filterIsInstance<JspJavaInjectionHost>()
        assertEquals(4, hosts.size)
        for (host in hosts) {
            assertFalse(host.text, InjectedLanguageUtil.isHighlightInjectionBackground(host))
            val injected = InjectedLanguageManager.getInstance(project).getInjectedPsiFiles(host)
            assertTrue(host.text, injected.orEmpty().any { it.first is PsiJavaFile })
        }
        assertEquals(source, myFixture.editor.document.text)
    }

    fun testEmptyAndUnterminatedJavaRegions() {
        for (source in listOf("<%! %>", "<%!", "<%! int count;",
            "<% %>", "<%", "<% int count;", "<%= %>", "<%=", "<%= 1")) {
            myFixture.configureByText(JspFileType.INSTANCE, source)
            val host = myFixture.file.children.filterIsInstance<JspJavaInjectionHost>().single()
            assertFalse(source, InjectedLanguageUtil.isHighlightInjectionBackground(host))
            assertEquals(source, myFixture.editor.document.text)
        }
    }

    fun testUpdatedJavaRegionsKeepBackgroundPolicy() {
        for (source in listOf("<%! int count = 2; %>", "<% int count = 2; %>", "<%= 2 %>")) {
            myFixture.configureByText(JspFileType.INSTANCE, source)
            val host = myFixture.file.children.filterIsInstance<JspJavaInjectionHost>().single()
            val updated = source.replace("2", "3")
            WriteCommandAction.runWriteCommandAction(project) {
                val replacement = host.updateText(updated)
                assertFalse(InjectedLanguageUtil.isHighlightInjectionBackground(replacement))
                assertEquals(updated, replacement.text)
            }
        }
    }
}
