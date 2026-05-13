package com.piagent.launcher.services

import com.piagent.launcher.settings.PiSettings
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.content.ContentFactory
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JComponent
import javax.swing.Timer

/**
 * Manages the Pi terminal session lifecycle.
 * Uses reflection to support both legacy and reworked terminal APIs.
 */
@Service(Service.Level.PROJECT)
class PiTerminalService(private val project: Project) {

    private val logger = Logger.getInstance(PiTerminalService::class.java)
    private val terminalComponent = AtomicReference<JComponent?>(null)
    private var sendTextFn: ((String, Boolean) -> Unit)? = null
    private var isInitialized = false

    /**
     * Initialize the Pi terminal inside the given tool window.
     */
    fun initTerminal(toolWindow: ToolWindow) {
        if (isInitialized) {
            toolWindow.show()
            return
        }

        toolWindow.contentManager.removeAllContents(true)

        try {
            val workingDir = project.basePath ?: System.getProperty("user.home")

            if (tryReworkedTerminal(toolWindow, workingDir)) {
                logger.info("Pi terminal initialized with Reworked Terminal API")
            } else {
                initLegacyTerminal(toolWindow, workingDir)
                logger.info("Pi terminal initialized with Legacy Terminal API")
            }

            isInitialized = true
            startPiWithDelay()
        } catch (e: Exception) {
            logger.error("Failed to initialize Pi terminal", e)
        }
    }

    /**
     * Try to use the Reworked Terminal API (2025.3+).
     * Returns true if successful.
     */
    private fun tryReworkedTerminal(toolWindow: ToolWindow, workingDir: String): Boolean {
        return try {
            // Check if TerminalToolWindowTabsManager exists
            val managerClass = Class.forName(
                "com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTabsManager"
            )

            val getInstanceMethod = managerClass.getMethod("getInstance", Project::class.java)
            val manager = getInstanceMethod.invoke(null, project) ?: return false

            // createTabBuilder()
            val createTabBuilderMethod = managerClass.getMethod("createTabBuilder")
            val tabBuilder = createTabBuilderMethod.invoke(manager) ?: return false

            // Set working directory if method exists
            try {
                val setDirMethod = tabBuilder.javaClass.getMethod("setWorkingDirectory", String::class.java)
                setDirMethod.invoke(tabBuilder, workingDir)
            } catch (_: NoSuchMethodException) {}

            // Build the tab - returns Content with TerminalView
            val buildMethod = tabBuilder.javaClass.getMethod("build")
            val content = buildMethod.invoke(tabBuilder)

            // Get TerminalView from the content
            val terminalViewClass = Class.forName(
                "com.intellij.terminal.frontend.view.TerminalView"
            )

            // Store sendText function via reflection
            val sendTextMethod = terminalViewClass.getMethod("sendText", String::class.java)
            val createSendTextBuilderMethod = try {
                terminalViewClass.getMethod("createSendTextBuilder")
            } catch (_: NoSuchMethodException) { null }

            // Get the TerminalView instance from content
            // The tab is created in the Terminal tool window, we need to get its component
            // This approach creates a tab in the IDE's Terminal window, not our custom one
            // So we fall back to legacy for custom tool window embedding
            return false
        } catch (_: ClassNotFoundException) {
            false
        } catch (_: NoSuchMethodException) {
            false
        } catch (e: Exception) {
            logger.debug("Reworked Terminal API not available: ${e.message}")
            false
        }
    }

    /**
     * Legacy terminal initialization using LocalTerminalDirectRunner.
     */
    @Suppress("DEPRECATION")
    private fun initLegacyTerminal(toolWindow: ToolWindow, workingDir: String) {
        val runnerClass = Class.forName("org.jetbrains.plugins.terminal.LocalTerminalDirectRunner")
        val createMethod = runnerClass.getMethod("createTerminalRunner", Project::class.java)
        val runner = createMethod.invoke(null, project)

        val createWidgetMethod = runner.javaClass.getMethod(
            "createTerminalWidget",
            com.intellij.openapi.Disposable::class.java,
            String::class.java,
            Boolean::class.javaPrimitiveType
        )
        val widget = createWidgetMethod.invoke(runner, toolWindow.disposable, workingDir, true)

        // Get component
        val component = widget.javaClass.getMethod("getComponent").invoke(widget) as JComponent
        terminalComponent.set(component)

        // Store sendText function
        sendTextFn = { text, execute ->
            try {
                val starter = widget.javaClass.getMethod("getTerminalStarter").invoke(widget)
                if (starter != null) {
                    val sendStr = if (execute) text + "\n" else text
                    starter.javaClass.getMethod("sendString", String::class.java, Boolean::class.javaPrimitiveType)
                        .invoke(starter, sendStr, false)
                }
            } catch (e: Exception) {
                logger.warn("Failed to send text: ${e.message}")
            }
        }

        val content = ContentFactory.getInstance().createContent(component, "Pi", true)
        content.isCloseable = true
        toolWindow.contentManager.addContent(content)
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

            // Disable themes to avoid unsupported escape sequences in embedded terminal
            append(" --no-themes")

            if (settings.extraArgs.isNotBlank()) {
                append(" ")
                append(settings.extraArgs)
            }
        }

        // Poll until terminal is ready, then send command
        var attempts = 0
        val timer = object : Timer(200, null) {
            init {
                isRepeats = true
                addActionListener {
                    attempts++
                    val fn = sendTextFn
                    if (fn != null) {
                        fn(command, true)
                        stop()
                    } else if (attempts > 25) {
                        logger.warn("Terminal not ready after 5s")
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
        terminalComponent.get()?.requestFocusInWindow()
    }

    fun isReady(): Boolean = terminalComponent.get() != null && isInitialized

    /**
     * Reset state so terminal can be re-initialized after close.
     */
    fun reset() {
        try {
            // The terminal process is killed when the disposable is disposed
            terminalComponent.set(null)
            sendTextFn = null
        } catch (_: Exception) {}
        isInitialized = false
        logger.info("Pi terminal reset")
    }

    companion object {
        fun getInstance(project: Project): PiTerminalService = project.service()
    }
}
