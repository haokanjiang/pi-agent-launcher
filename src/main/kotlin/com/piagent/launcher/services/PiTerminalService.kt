package com.piagent.launcher.services

import com.piagent.launcher.settings.PiSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.terminal.JBTerminalWidget
import com.intellij.ui.content.ContentFactory
import org.jetbrains.plugins.terminal.LocalTerminalDirectRunner
import java.util.concurrent.atomic.AtomicReference
import javax.swing.Timer

/**
 * Manages the Pi terminal session lifecycle.
 */
@Service(Service.Level.PROJECT)
class PiTerminalService(private val project: Project) {

    private val logger = Logger.getInstance(PiTerminalService::class.java)
    private val terminalWidget = AtomicReference<JBTerminalWidget?>(null)
    private var isInitialized = false

    /**
     * Initialize the Pi terminal inside the given tool window.
     */
    fun initTerminal(toolWindow: ToolWindow) {
        if (isInitialized) {
            toolWindow.show()
            return
        }

        // Clear any leftover content
        toolWindow.contentManager.removeAllContents(true)

        try {
            val workingDir = project.basePath ?: System.getProperty("user.home")
            val runner = LocalTerminalDirectRunner.createTerminalRunner(project)
            val widget = runner.createTerminalWidget(toolWindow.disposable, workingDir, true)
            terminalWidget.set(widget)

            val content = ContentFactory.getInstance().createContent(
                widget.component, "Pi", true
            )
            content.isCloseable = true
            toolWindow.contentManager.addContent(content)

            isInitialized = true
            logger.info("Pi terminal initialized in $workingDir")

            // Delay sending pi command to wait for shell to be ready
            startPiWithDelay(widget)
        } catch (e: Exception) {
            logger.error("Failed to initialize Pi terminal", e)
        }
    }

    private fun startPiWithDelay(widget: JBTerminalWidget) {
        val settings = PiSettings.getInstance().state
        val command = buildString {
            append(settings.piCommand)

            // Model: custom id takes priority over dropdown
            val model = if (settings.customModelId.isNotBlank()) {
                settings.customModelId
            } else if (settings.model != "Default") {
                settings.model
            } else null

            if (model != null) {
                append(" --model $model")
            }

            // Thinking level
            if (settings.thinkingLevel != "Default" && settings.thinkingLevel.isNotBlank()) {
                append(" --thinking ${settings.thinkingLevel}")
            }

            // Disable themes to avoid unsupported escape sequences in embedded terminal
            append(" --no-themes")

            if (settings.extraArgs.isNotBlank()) {
                append(" ")
                append(settings.extraArgs)
            }
            append("\n")
        }

        // Poll until terminalStarter is ready, then send command
        var attempts = 0
        val timer = object : Timer(200, null) {
            init {
                isRepeats = true
                addActionListener {
                    attempts++
                    val starter = widget.terminalStarter
                    if (starter != null) {
                        starter.sendString(command, false)
                        stop()
                    } else if (attempts > 25) {
                        logger.warn("Terminal starter not ready after 5s")
                        stop()
                    }
                }
            }
        }
        timer.start()
    }

    /**
     * Send text to the Pi terminal and execute (press Enter).
     */
    fun sendText(text: String) {
        val widget = terminalWidget.get() ?: return
        widget.terminalStarter?.sendString(text + "\n", false)
    }

    /**
     * Insert text into the terminal input without executing.
     */
    fun insertText(text: String) {
        val widget = terminalWidget.get() ?: return
        widget.terminalStarter?.sendString(text, false)
    }

    /**
     * Focus the terminal widget.
     */
    fun focus() {
        terminalWidget.get()?.component?.requestFocusInWindow()
    }

    fun isReady(): Boolean = terminalWidget.get() != null && isInitialized

    /**
     * Reset state so terminal can be re-initialized after close.
     */
    fun reset() {
        val widget = terminalWidget.get()
        try {
            widget?.ttyConnector?.close()
        } catch (_: Exception) {
        }
        terminalWidget.set(null)
        isInitialized = false
        logger.info("Pi terminal reset")
    }

    companion object {
        fun getInstance(project: Project): PiTerminalService = project.service()
    }
}
