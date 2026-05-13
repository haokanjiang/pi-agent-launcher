package com.piagent.launcher.actions

import com.piagent.launcher.services.PiTerminalService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.wm.ToolWindowManager

/**
 * Toggle Pi Agent tool window visibility.
 * Cmd+Esc to open/hide.
 */
class OpenPiAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Pi Agent") ?: return

        if (toolWindow.isVisible) {
            toolWindow.hide()
        } else {
            val service = PiTerminalService.getInstance(project)

            // Re-initialize if terminal was closed
            if (!service.isReady()) {
                service.initTerminal(toolWindow)
            }

            toolWindow.show {
                service.focus()
            }
        }
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = e.project != null
    }
}
