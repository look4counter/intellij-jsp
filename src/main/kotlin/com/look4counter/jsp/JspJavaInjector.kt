package com.look4counter.jsp

import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.util.TextRange
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement

class JspJavaInjector : MultiHostInjector {
    override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(JspJavaInjectionHost::class.java)

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val host = context as JspJavaInjectionHost
        val file = host.containingFile
        val hosts = file.children.filterIsInstance<JspJavaInjectionHost>().filter { !it.bodyRange.isEmpty }
        if (host !in hosts) return
        val unit = JspIncludeContext.build(file)
        val declarationContext = host.kind == JspTokenTypes.DECLARATION
        val declarations = unit.parts.filter { it.kind == JspTokenTypes.DECLARATION }
        val ordered = unit.parts.filter { (it.kind == JspTokenTypes.DECLARATION) == declarationContext }
        val local = hosts.associateBy { it.textRange.startOffset }
        val namespace = if (!DumbService.isDumb(host.project) && JavaPsiFacade.getInstance(host.project)
                .findClass("jakarta.servlet.http.HttpServletRequest", host.resolveScope) != null) "jakarta" else "javax"
        val spans = mutableListOf<JspJavaSourceMap.Span>()
        fun StringBuilder.appendSource(part: JspIncludeContext.Part) {
            val start = length
            append(part.body)
            val delimiter = if (part.kind == JspTokenTypes.SCRIPTLET) 2 else 3
            spans += JspJavaSourceMap.Span(TextRange(start, length), part.file, part.offset + delimiter)
        }
        val code = StringBuilder().apply {
            append("import java.lang.*;\n")
            for (name in unit.imports) append("import $name;\n")
            append("import $namespace.servlet.*;\nimport $namespace.servlet.http.*;\nimport $namespace.servlet.jsp.*;\n")
            append("class __JspPage {\n")
            append("$namespace.servlet.http.HttpServletRequest request;\n")
            append("$namespace.servlet.http.HttpServletResponse response;\n")
            append("$namespace.servlet.http.HttpSession session;\n")
            append("$namespace.servlet.ServletContext application;\n")
            append("$namespace.servlet.ServletConfig config;\n")
            append("$namespace.servlet.jsp.PageContext pageContext;\n")
            append("$namespace.servlet.jsp.JspWriter out;\nObject page = this;\n")
            if (!declarationContext) {
                for (part in declarations) { appendSource(part); append('\n') }
                append("void __jspService() throws Throwable {\n")
            }
        }
        val places = mutableListOf<Place>()
        var pendingStart = 0
        for (part in ordered) {
            val expression = part.kind == JspTokenTypes.EXPRESSION
            if (expression) code.append("java.lang.String.valueOf(")
            val bodyStart = code.length
            code.appendSource(part)
            val suffix = if (expression) "\n);\n" else "\n"
            code.append(suffix)
            val editable = if (part.file == unit.currentFile) local[part.offset] else null
            if (editable != null) {
                places += Place(editable, code.substring(pendingStart, bodyStart), suffix)
                pendingStart = code.length
            }
        }
        if (places.isEmpty()) return
        code.append(if (declarationContext) "}\n" else "}\n}\n")
        places.last().suffix += code.substring(pendingStart)
        registrar.startInjecting(JavaLanguage.INSTANCE)
        registrar.putInjectedFileUserData(JspJavaSourceMap.KEY, spans.toList())
        for (place in places) registrar.addPlace(place.prefix, place.suffix, place.host, place.host.bodyRange)
        registrar.doneInjecting()
    }

    private data class Place(val host: JspJavaInjectionHost, val prefix: String, var suffix: String)
}
