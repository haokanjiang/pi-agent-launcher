package com.piagent.launcher.actions

import com.piagent.launcher.services.PiSessionService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * Launch Pi, or focus it when it is already running (Cmd/Ctrl+Esc).
 */
class OpenPiAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        PiSessionService.getInstance(project).launch()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
