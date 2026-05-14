package com.piagent.launcher.actions

import com.piagent.launcher.services.PiTerminalService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Toggle/focus Pi terminal tab.
 * Cmd+Esc to launch or focus.
 */
class OpenPiAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val service = PiTerminalService.getInstance(project)
        service.launch()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
