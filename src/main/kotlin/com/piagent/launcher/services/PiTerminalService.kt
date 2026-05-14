package com.piagent.launcher.services

import com.piagent.launcher.settings.PiSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JComponent
import javax.swing.Timer

/**
 * Manages the Pi terminal session lifecycle.
 * Creates a "Pi" tab inside the IDE's Terminal tool window.
 */
@Service(Service.Level.PROJECT)
class PiTerminalService(private val project: Project) {

    private val logger = Logger.getInstance(PiTerminalService::class.java)
    private val terminalComponent = AtomicReference<JComponent?>(null)
    private var sendTextFn: ((String, Boolean) -> Unit)? = null
    private var isInitialized = false

    /**
     * Launch Pi as a new tab in the IDE's Terminal tool window.
     * Only one Pi session at a time — if already running, focus it.
     */
    fun launch() {
        if (isInitialized && isTerminalAlive()) {
            focusTerminalWindow()
            return
        }

        if (isInitialized) {
            reset()
        }

        try {
            val workingDir = project.basePath ?: System.getProperty("user.home")
            createTerminalTab(workingDir)
            isInitialized = true
            startPiWithDelay()
        } catch (e: Exception) {
            logger.error("Failed to launch Pi terminal", e)
        }
    }

    /**
     * Check if the Pi terminal tab is still alive.
     */
    private fun isTerminalAlive(): Boolean {
        val component = terminalComponent.get() ?: return false
        // Check if component is still attached to a parent (tab exists even if not focused)
        return component.parent != null
    }

    /**
     * Create a new tab named "Pi" in the Terminal tool window.
     */
    @Suppress("DEPRECATION")
    private fun createTerminalTab(workingDir: String) {
        try {
            // Use TerminalToolWindowManager to create a tab in the Terminal window
            val managerClass = Class.forName("org.jetbrains.plugins.terminal.TerminalToolWindowManager")
            val getInstanceMethod = managerClass.getMethod("getInstance", Project::class.java)
            val manager = getInstanceMethod.invoke(null, project)

            // createLocalShellWidget(workingDir, tabName) -> ShellTerminalWidget
            val createMethod = managerClass.getMethod(
                "createLocalShellWidget",
                String::class.java,
                String::class.java
            )
            val widget = createMethod.invoke(manager, workingDir, "Pi")

            // Get component
            val component = widget.javaClass.getMethod("getComponent").invoke(widget) as JComponent
            terminalComponent.set(component)

            // Store sendText function via ShellTerminalWidget
            sendTextFn = { text, execute ->
                try {
                    if (execute) {
                        widget.javaClass.getMethod("executeCommand", String::class.java)
                            .invoke(widget, text)
                    } else {
                        val starter = widget.javaClass.getMethod("getTerminalStarter").invoke(widget)
                        if (starter != null) {
                            starter.javaClass.getMethod("sendString", String::class.java, Boolean::class.javaPrimitiveType)
                                .invoke(starter, text, false)
                        }
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to send text: ${e.message}")
                }
            }

            // Show and focus the Terminal tool window
            focusTerminalWindow()

            logger.info("Pi terminal tab created in Terminal window")
        } catch (e: Exception) {
            logger.error("Failed to create terminal tab", e)
            throw e
        }
    }

    private fun focusTerminalWindow() {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Terminal")
        toolWindow?.show {
            // Find and select the Pi tab
            val contentManager = toolWindow.contentManager
            for (content in contentManager.contents) {
                if (content.displayName == "Pi") {
                    contentManager.setSelectedContent(content, true)
                    break
                }
            }
            terminalComponent.get()?.requestFocusInWindow()
        }
    }

    private fun startPiWithDelay() {
        val settings = PiSettings.getInstance().state
        val command = buildString {
            append(settings.piCommand)

            val model = if (settings.customModelId.isNotBlank()) {
                settings.customModelId
            } else if (settings.model != "Default") {
                settings.model
            } else null

            if (model != null) {
                append(" --model $model")
            }

            if (settings.thinkingLevel != "Default" && settings.thinkingLevel.isNotBlank()) {
                append(" --thinking ${settings.thinkingLevel}")
            }

            append(" --no-themes")

            if (settings.extraArgs.isNotBlank()) {
                append(" ")
                append(settings.extraArgs)
            }
        }

        // Poll until terminal is ready, then send command
        var attempts = 0
        val timer = object : Timer(300, null) {
            init {
                isRepeats = true
                addActionListener {
                    attempts++
                    val fn = sendTextFn
                    if (fn != null) {
                        fn(command, true)
                        stop()
                    } else if (attempts > 20) {
                        logger.warn("Terminal not ready after 6s")
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
        sendTextFn?.invoke(text, true)
    }

    /**
     * Insert text into the terminal input without executing.
     */
    fun insertText(text: String) {
        sendTextFn?.invoke(text, false)
    }

    /**
     * Focus the terminal widget.
     */
    fun focus() {
        focusTerminalWindow()
    }

    fun isReady(): Boolean = isInitialized

    /**
     * Reset state so terminal can be re-launched.
     */
    fun reset() {
        terminalComponent.set(null)
        sendTextFn = null
        isInitialized = false
        logger.info("Pi terminal reset")
    }

    companion object {
        fun getInstance(project: Project): PiTerminalService = project.service()
    }
}
