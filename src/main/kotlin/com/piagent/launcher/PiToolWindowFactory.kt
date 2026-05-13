package com.piagent.launcher

import com.piagent.launcher.services.PiTerminalService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener

/**
 * Creates the Pi Agent tool window with an embedded terminal running `pi`.
 */
class PiToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.setToHideOnEmptyContent(true)

        val service = PiTerminalService.getInstance(project)
        service.initTerminal(toolWindow)

        // When tab is closed, gracefully exit pi then hide tool window
        toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
            override fun contentRemoveQuery(event: ContentManagerEvent) {
                // Send exit before content is actually removed
                service.reset()
            }

            override fun contentRemoved(event: ContentManagerEvent) {
                toolWindow.hide()
            }
        })
    }

    override fun shouldBeAvailable(project: Project): Boolean = true
}
