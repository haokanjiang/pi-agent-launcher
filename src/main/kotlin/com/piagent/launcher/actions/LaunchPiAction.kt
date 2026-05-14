package com.piagent.launcher.actions

import com.piagent.launcher.services.PiFileWatcher
import com.piagent.launcher.services.PiTerminalService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * One-click launch Pi as a tab in the Terminal tool window.
 */
class LaunchPiAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = PiTerminalService.getInstance(project)
        service.launch()
        PiFileWatcher.getInstance(project).startWatching()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
