package com.piagent.launcher.services

import com.intellij.openapi.Disposable
import com.piagent.launcher.infrastructure.PiTerminal
import javax.swing.Timer

/**
 * Schedules delayed work, returning a handle that cancels it.
 *
 * Exists so the session logic can be tested without real Swing timers.
 */
internal fun interface PiScheduler {
    fun schedule(delayMs: Int, repeat: Boolean, action: () -> Unit): Disposable

    companion object {
        /** Default scheduler: Swing timers, so callbacks land on the EDT. */
        val swing: PiScheduler = PiScheduler { delayMs, repeat, action ->
            val timer = Timer(delayMs) { action() }
            timer.isRepeats = repeat
            timer.start()
            object : Disposable {
                override fun dispose() = timer.stop()
            }
        }
    }
}

/**
 * Submits the Pi start command to a freshly created terminal tab.
 *
 * The terminal component exists before its shell is attached and the widget
 * offers no readiness signal, so the command is sent after a short settle delay.
 */
internal class PiCommandDispatcher(
    private val settleMillis: Int = SETTLE_MILLIS,
    private val scheduler: PiScheduler = PiScheduler.swing
) {
    fun submit(terminal: PiTerminal, command: String, onSent: () -> Unit = {}) {
        scheduler.schedule(settleMillis, repeat = false) {
            terminal.send(command, submit = true)
            onSent()
        }
    }

    companion object {
        const val SETTLE_MILLIS = 300
    }
}
