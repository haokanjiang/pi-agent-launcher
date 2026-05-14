package com.piagent.launcher.actions

import com.piagent.launcher.services.PiTerminalService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

/**
 * Send file(s) from Project View to Pi.
 * Supports single and multiple file selection.
 * Format: @relative/path/to/file.go
 */
class SendFileToPiAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val virtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY) ?: return

        if (virtualFiles.isEmpty()) return

        val projectPath = project.basePath ?: ""

        // Build references for all selected files
        val references = virtualFiles.map { file ->
            val relativePath = if (file.path.startsWith(projectPath)) {
                file.path.removePrefix(projectPath).removePrefix("/")
            } else {
                file.path
            }
            "@$relativePath"
        }.joinToString(" ")

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
        val files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)
        e.presentation.isVisible = true
        e.presentation.isEnabled = files != null && files.isNotEmpty()
    }
}
