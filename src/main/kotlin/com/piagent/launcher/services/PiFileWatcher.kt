package com.piagent.launcher.services

import com.piagent.launcher.settings.PiSettings
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.messages.MessageBusConnection

/**
 * Watches for file changes made by Pi.
 * - Auto-opens modified files in the editor
 * - Shows notification when Pi finishes modifying files
 */
@Service(Service.Level.PROJECT)
class PiFileWatcher(private val project: Project) : Disposable {

    private val logger = Logger.getInstance(PiFileWatcher::class.java)
    private val modifiedFiles = mutableSetOf<String>()
    private var isWatching = false
    private var connection: MessageBusConnection? = null

    fun startWatching() {
        if (isWatching) return
        isWatching = true
        modifiedFiles.clear()

        // Disconnect previous listener if any
        connection?.disconnect()

        connection = project.messageBus.connect(this).also { conn ->
            conn.subscribe(
                com.intellij.openapi.vfs.VirtualFileManager.VFS_CHANGES,
                object : BulkFileListener {
                    override fun after(events: List<VFileEvent>) {
                        if (!isWatching) return
                        val settings = PiSettings.getInstance().state

                        for (event in events) {
                            if (event is VFileContentChangeEvent) {
                                val file = event.file
                                val projectPath = project.basePath ?: continue

                                if (file.path.startsWith(projectPath)) {
                                    modifiedFiles.add(file.path)

                                    if (settings.autoOpenFiles) {
                                        openFileInEditor(file)
                                    }
                                }
                            }
                        }
                    }
                }
            )
        }

        logger.info("Pi file watcher started")
    }

    fun stopWatching() {
        isWatching = false
        connection?.disconnect()
        connection = null

        val settings = PiSettings.getInstance().state
        if (settings.showNotifications && modifiedFiles.isNotEmpty()) {
            showCompletionNotification()
        }

        modifiedFiles.clear()
    }

    private fun openFileInEditor(file: VirtualFile) {
        FileEditorManager.getInstance(project).openFile(file, false)
    }

    private fun showCompletionNotification() {
        val count = modifiedFiles.size
        val message = if (count == 1) {
            "Pi modified 1 file"
        } else {
            "Pi modified $count files"
        }

        NotificationGroupManager.getInstance()
            .getNotificationGroup("Pi Agent")
            .createNotification(message, NotificationType.INFORMATION)
            .notify(project)
    }

    override fun dispose() {
        stopWatching()
    }

    companion object {
        fun getInstance(project: Project): PiFileWatcher = project.service()
    }
}
