package com.piagent.launcher.actions

import com.piagent.launcher.services.PiDiffWatcher
import com.piagent.launcher.services.PiTerminalService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

/**
 * Send selected code to Pi as a file reference.
 * Format: @relative/path/to/file.go#L10-25
 */
class SendSelectionAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val selectionModel = editor.selectionModel
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: e.getData(CommonDataKeys.PSI_FILE)?.virtualFile
            ?: return

        // Calculate line range
        val startLine: Int
        val endLine: Int

        if (selectionModel.hasSelection()) {
            startLine = editor.document.getLineNumber(selectionModel.selectionStart) + 1
            endLine = editor.document.getLineNumber(selectionModel.selectionEnd) + 1
        } else {
            // No selection — use entire file
            startLine = 1
            endLine = editor.document.lineCount
        }

        // Build relative path from project root
        val projectPath = project.basePath ?: ""
        val filePath = virtualFile.path
        val relativePath = if (filePath.startsWith(projectPath)) {
            filePath.removePrefix(projectPath).removePrefix("/")
        } else {
            filePath
        }

        // Build reference: @path/to/file.go#L10-25
        val reference = if (startLine == endLine) {
            "@$relativePath#L$startLine "
        } else {
            "@$relativePath#L$startLine-$endLine "
        }

        // Ensure Pi is launched
        val service = PiTerminalService.getInstance(project)
        if (!service.isReady()) {
            service.launch()
        }

        // Start diff watching
        val diffWatcher = PiDiffWatcher.getInstance(project)
        diffWatcher.snapshotFile(filePath)
        diffWatcher.startWatching()

        // Insert reference into terminal input (don't execute)
        service.insertText(reference)
        service.focus()
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isVisible = true
        e.presentation.isEnabled = editor != null
    }
}
