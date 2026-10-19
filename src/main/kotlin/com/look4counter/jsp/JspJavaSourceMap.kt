package com.look4counter.jsp

import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile

object JspJavaSourceMap {
    data class Span(val generated: TextRange, val file: VirtualFile, val sourceStart: Int)
    val KEY = Key.create<List<Span>>("community.jsp.java.source.map")
}
