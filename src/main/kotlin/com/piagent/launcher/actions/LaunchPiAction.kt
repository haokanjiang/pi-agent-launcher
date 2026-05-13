package com.piagent.launcher.actions

import com.piagent.launcher.services.PiFileWatcher
import com.piagent.launcher.services.PiTerminalService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

/**
 * One-click launch Pi from toolbar.
 * - No window: create and show
 * - Window hidden: show and focus
 * - Window visible: focus it
 */
class LaunchPiAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Pi Agent") ?: return
        val service = PiTerminalService.getInstance(project)

        // Re-initialize if terminal was closed
        if (!service.isReady()) {
            service.initTerminal(toolWindow)
            PiFileWatcher.getInstance(project).startWatching()
        }

        // Show and focus
        toolWindow.show {
            service.focus()
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
