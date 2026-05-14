package com.piagent.launcher.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.Consumer
import java.awt.event.MouseEvent
import javax.swing.Icon

/**
 * Status bar widget showing Pi running state.
 */
class PiStatusWidgetFactory : StatusBarWidgetFactory {

    override fun getId(): String = "PiAgentStatus"
    override fun getDisplayName(): String = "Pi Agent Status"
    override fun isAvailable(project: Project): Boolean = true

    override fun createWidget(project: Project): StatusBarWidget {
        return PiStatusWidget(project)
    }
}

class PiStatusWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation {

    private var statusBar: StatusBar? = null

    override fun ID(): String = "PiAgentStatus"

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun getText(): String {
        val service = PiTerminalService.getInstance(project)
        return if (service.isReady()) "π Running" else "π Idle"
    }

    override fun getTooltipText(): String {
        val service = PiTerminalService.getInstance(project)
        return if (service.isReady()) "Pi Agent is running. Click to focus." else "Pi Agent is idle. Click to launch."
    }

    override fun getClickConsumer(): Consumer<MouseEvent> = Consumer {
        PiTerminalService.getInstance(project).launch()
    }

    override fun getAlignment(): Float = 0f

    override fun dispose() {
        statusBar = null
    }

    companion object {
        fun update(project: Project) {
            val statusBar = WindowManager.getInstance().getStatusBar(project) ?: return
            statusBar.updateWidget("PiAgentStatus")
        }
    }
}
