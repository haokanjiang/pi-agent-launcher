package com.piagent.launcher.services

import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.Topic
import com.piagent.launcher.infrastructure.PiTerminal

/**
 * Observable state of the Pi session.
 *
 * The status bar widget listens to this instead of reaching into the session
 * service, which removes the previous circular dependency between the two.
 */
enum class PiSessionStatus { IDLE, RUNNING }

fun interface PiSessionListener {
    fun sessionStatusChanged(status: PiSessionStatus)

    companion object {
        @JvmField
        val TOPIC: Topic<PiSessionListener> = Topic.create(
            "Pi session status",
            PiSessionListener::class.java
        )
    }
}

/**
 * Holds the current terminal/status and publishes changes on the project bus.
 */
internal class PiSessionState(private val publisher: (PiSessionStatus) -> Unit) {

    @Volatile
    var status: PiSessionStatus = PiSessionStatus.IDLE
        private set

    @Volatile
    var terminal: PiTerminal? = null
        private set

    fun markRunning(terminal: PiTerminal) {
        this.terminal = terminal
        transitionTo(PiSessionStatus.RUNNING)
    }

    fun markIdle() {
        terminal = null
        transitionTo(PiSessionStatus.IDLE)
    }

    private fun transitionTo(next: PiSessionStatus) {
        if (status == next) return
        status = next
        publisher(next)
    }

    companion object {
        /** Publishes on the project message bus, safely from any thread. */
        fun publisherFor(project: com.intellij.openapi.project.Project): (PiSessionStatus) -> Unit = { status ->
            ApplicationManager.getApplication().invokeLater {
                if (!project.isDisposed) {
                    project.messageBus.syncPublisher(PiSessionListener.TOPIC).sessionStatusChanged(status)
                }
            }
        }
    }
}
