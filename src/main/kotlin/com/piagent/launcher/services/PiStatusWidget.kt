package com.piagent.launcher.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.WindowManager
import com.intellij.util.Consumer
import java.awt.event.MouseEvent

/**
 * Status bar entry that reflects [PiSessionService] state.
 *
 * The widget subscribes to [PiSessionListener] rather than reading the session
 * service, so there is no dependency from the service back to the UI.
 */
class PiStatusWidgetFactory : StatusBarWidgetFactory {

    override fun getId(): String = ID
    override fun getDisplayName(): String = "Pi Agent Status"
    override fun isAvailable(project: Project): Boolean = true
    override fun createWidget(project: Project): StatusBarWidget = PiStatusWidget(project)

    companion object {
        const val ID = "PiAgentStatus"
    }
}

class PiStatusWidget(private val project: Project) : StatusBarWidget, StatusBarWidget.TextPresentation, Disposable {

    private var statusBar: StatusBar? = null

    @Volatile
    private var status: PiSessionStatus = PiSessionStatus.IDLE

    private val connection = project.messageBus.connect(this).apply {
        subscribe(
            PiSessionListener.TOPIC,
            PiSessionListener { newStatus ->
                status = newStatus
                statusBar?.updateWidget(PiStatusWidgetFactory.ID)
            }
        )
    }

    override fun ID(): String = PiStatusWidgetFactory.ID

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun getPresentation(): StatusBarWidget.WidgetPresentation = this

    override fun getText(): String = when (status) {
        PiSessionStatus.RUNNING -> "π Running"
        PiSessionStatus.IDLE -> "π Idle"
    }

    override fun getTooltipText(): String = when (status) {
        PiSessionStatus.RUNNING -> "Pi Agent is running. Click to focus."
        PiSessionStatus.IDLE -> "Pi Agent is idle. Click to launch."
    }

    override fun getClickConsumer(): Consumer<MouseEvent> =
        Consumer { PiSessionService.getInstance(project).launch() }

    override fun getAlignment(): Float = 0f

    override fun dispose() {
        connection.disconnect()
        statusBar = null
    }

    companion object {
        /** Force a repaint of the widget for [project], if it is installed. */
        fun update(project: Project) {
            WindowManager.getInstance().getStatusBar(project)
                ?.updateWidget(PiStatusWidgetFactory.ID)
        }
    }
}
