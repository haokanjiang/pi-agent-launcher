package com.piagent.launcher.actions

import com.piagent.launcher.services.PiSessionService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

/**
 * One-click launch: starts Pi and focuses its terminal tab.
 */
class LaunchPiAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        PiSessionService.getInstance(project).launch()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
