package com.look4counter.jsp

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.tree.IElementType
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/** Builds read-only copies of included Java; editable hosts always stay in one file. */
object JspIncludeContext {
    data class Part(val file: VirtualFile, val offset: Int, val kind: IElementType, val body: String)
    data class TranslationUnit(val parts: List<Part>, val imports: Set<String>, val currentFile: VirtualFile)
    private data class Edge(val parent: VirtualFile, val offset: Int)
    private val REVERSE = Key.create<CachedValue<Map<VirtualFile, List<Edge>>>>("community.jsp.include.parents")
    private val IMPORT_NAME = Regex("[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*(?:\\.[\\p{javaJavaIdentifierStart}][\\p{javaJavaIdentifierPart}]*)*(?:\\.\\*)?")
    private const val MAX_DEPTH = 64
    private const val MAX_EXPANSIONS = 1024
    private const val MAX_CHARACTERS = 2_000_000

    fun build(file: PsiFile): TranslationUnit {
        val current = file.originalFile.virtualFile ?: file.viewProvider.virtualFile
        val parents = parents(file.project)
        var root = current
        val seen = mutableSetOf(current)
        while (true) {
            val edges = parents[root].orEmpty()
            if (edges.isEmpty()) break
            if (edges.size != 1 || seen.size >= MAX_DEPTH || !seen.add(edges.single().parent)) {
                root = current
                break
            }
            root = edges.single().parent
        }
        fun expand(start: VirtualFile): Pair<TranslationUnit, Int> {
            val parts = mutableListOf<Part>()
            val imports = linkedSetOf<String>()
            val active = mutableSetOf<VirtualFile>()
            var occurrences = 0
            var expansions = 0
            var characters = 0
            fun visit(source: VirtualFile, depth: Int) {
                ProgressManager.checkCanceled()
                if (depth > MAX_DEPTH || expansions >= MAX_EXPANSIONS || !active.add(source)) return
                expansions++
                if (source != current && source.length > MAX_CHARACTERS) {
                    active.remove(source)
                    return
                }
                val text = if (source == current) file.text else text(source, file.project) ?: ""
                if (characters + text.length > MAX_CHARACTERS && source != current) {
                    active.remove(source)
                    return
                }
                characters += text.length
                if (source == current) occurrences++
                val lexer = JspHighlightingLexer()
                lexer.start(text)
                while (lexer.tokenType != null) {
                    ProgressManager.checkCanceled()
                    val kind = lexer.tokenType!!
                    val token = text.substring(lexer.tokenStart, lexer.tokenEnd)
                    if (kind in JspParserDefinition.JAVA_TOKENS) {
                        val begin = if (kind == JspTokenTypes.SCRIPTLET) 2 else 3
                        val end = if (token.endsWith("%>")) token.length - 2 else token.length
                        parts += Part(source, lexer.tokenStart, kind, token.substring(begin.coerceAtMost(end), end))
                    } else if (kind == JspTokenTypes.DIRECTIVE) {
                        val directive = directive(token)
                        if (directive?.name == "include") {
                            resolveInclude(source, directive.attributes["file"]?.value, file.project)?.let { visit(it, depth + 1) }
                        } else if (directive?.name == "page") {
                            directive.attributes["import"]?.value?.split(',')?.map(String::trim)
                                ?.filter(IMPORT_NAME::matches)?.let(imports::addAll)
                        }
                    }
                    lexer.advance()
                }
                active.remove(source)
            }
            visit(start, 0)
            return TranslationUnit(parts, imports, current) to occurrences
        }
        val (unit, occurrences) = expand(root)
        return if (root != current && occurrences != 1) expand(current).first else unit
    }

    private fun parents(project: Project): Map<VirtualFile, List<Edge>> {
        if (DumbService.isDumb(project)) return emptyMap()
        return CachedValuesManager.getManager(project).getCachedValue(project, REVERSE, {
            val reverse = mutableMapOf<VirtualFile, MutableList<Edge>>()
            for (source in FileTypeIndex.getFiles(JspFileType.INSTANCE, GlobalSearchScope.projectScope(project))) {
                ProgressManager.checkCanceled()
                val text = text(source, project) ?: continue
                val lexer = JspHighlightingLexer()
                lexer.start(text)
                while (lexer.tokenType != null) {
                    if (lexer.tokenType == JspTokenTypes.DIRECTIVE) {
                        val directive = directive(text.substring(lexer.tokenStart, lexer.tokenEnd))
                        if (directive?.name == "include") {
                            resolveInclude(source, directive.attributes["file"]?.value, project)?.let { target ->
                                reverse.getOrPut(target) { mutableListOf() } += Edge(source, lexer.tokenStart)
                            }
                        }
                    }
                    lexer.advance()
                }
            }
            CachedValueProvider.Result.create(reverse.mapValues { it.value.toList() },
                PsiModificationTracker.MODIFICATION_COUNT, VirtualFileManager.VFS_STRUCTURE_MODIFICATIONS,
                ProjectRootManager.getInstance(project), DumbService.getInstance(project).modificationTracker)
        }, false)
    }

    private fun text(file: VirtualFile, project: Project): String? =
        if (!file.isValid || file.isDirectory) null else PsiManager.getInstance(project).findFile(file)?.viewProvider?.contents?.toString()

    private fun directive(token: String) = JspDirectiveSyntax.parse(token, requireClosed = true)

    internal fun resolveInclude(source: VirtualFile, path: String?, project: Project): VirtualFile? {
        if (path.isNullOrEmpty() || path.contains("\${") || path.contains("#{") || path.contains("<%")) return null
        val decoded = try { URLDecoder.decode(path.replace("+", "%2B"), StandardCharsets.UTF_8) }
            catch (_: IllegalArgumentException) { return null }
        if (decoded.any { it in "\\:?#\u0000" }) return null
        val content = ProjectFileIndex.getInstance(project).getContentRootForFile(source)
        val base = if (decoded.startsWith('/')) webRoot(source, content) else source.parent
        val target = base?.findFileByRelativePath(decoded.removePrefix("/")) ?: return null
        if (target.isDirectory || (content != null && !VfsUtilCore.isAncestor(content, target, false))) return null
        return target
    }

    private fun webRoot(source: VirtualFile, content: VirtualFile?): VirtualFile? {
        if (content == null) return null
        var directory = source.parent
        while (directory != null && VfsUtilCore.isAncestor(content, directory, false)) {
            if (directory.name in setOf("webapp", "WebContent", "web") || directory.findChild("WEB-INF")?.isDirectory == true) return directory
            if (directory == content) break
            directory = directory.parent
        }
        return content
    }
}
