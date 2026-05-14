package com.piagent.launcher.actions

import com.piagent.launcher.services.PiTerminalService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileSystemItem

/**
 * Send file(s) from Project View to Pi.
 * Supports single and multiple file selection.
 * Format: @relative/path/to/file.go
 */
class SendFileToPiAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFiles = getFiles(e)

        if (virtualFiles.isEmpty()) return

        val projectPath = project.basePath ?: ""

        // Build references for all selected files (skip directories)
        val references = virtualFiles
            .filter { !it.isDirectory }
            .map { file ->
                val relativePath = if (file.path.startsWith(projectPath)) {
                    file.path.removePrefix(projectPath).removePrefix("/")
                } else {
                    file.path
                }
                "@$relativePath"
            }.joinToString(" ")

        if (references.isBlank()) return

        // Ensure Pi is launched
        val service = PiTerminalService.getInstance(project)
        if (!service.isReady()) {
            service.launch()
        }

        // Insert references (with trailing space for user to type question)
        service.insertText("$references ")
        service.focus()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isVisible = true
        val files = getFiles(e)
        e.presentation.isEnabled = files.any { !it.isDirectory }
    }

    private fun getFiles(e: AnActionEvent): List<VirtualFile> {
        // Try array first (multi-select)
        val array = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        if (array != null && array.isNotEmpty()) return array.toList()

        // Try single file
        val single = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (single != null) return listOf(single)

        // Try PSI elements (Project View uses this)
        val psiElements = e.getData(LangDataKeys.PSI_ELEMENT_ARRAY)
        if (psiElements != null) {
            val files = psiElements.mapNotNull { 
                (it as? PsiFileSystemItem)?.virtualFile 
            }
            if (files.isNotEmpty()) return files
        }

        // Try PSI file
        val psiFile = e.getData(CommonDataKeys.PSI_FILE)
        if (psiFile?.virtualFile != null) return listOf(psiFile.virtualFile)

        return emptyList()
    }
}
