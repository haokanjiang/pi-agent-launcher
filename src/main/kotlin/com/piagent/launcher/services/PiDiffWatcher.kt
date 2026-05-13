package com.piagent.launcher.services

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.messages.MessageBusConnection
import java.util.concurrent.ConcurrentHashMap

/**
 * Watches for file changes made by Pi and shows diff previews in the IDE.
 */
@Service(Service.Level.PROJECT)
class PiDiffWatcher(private val project: Project) : Disposable {

    private val logger = Logger.getInstance(PiDiffWatcher::class.java)
    private val snapshots = ConcurrentHashMap<String, String>()
    private var isWatching = false
    private var connection: MessageBusConnection? = null

    fun startWatching() {
        if (isWatching) return
        isWatching = true

        snapshotOpenFiles()

        connection?.disconnect()

        connection = project.messageBus.connect(this).also { conn ->
            conn.subscribe(
                com.intellij.openapi.vfs.VirtualFileManager.VFS_CHANGES,
                object : BulkFileListener {
                    override fun after(events: List<VFileEvent>) {
                        if (!isWatching) return
                        for (event in events) {
                            if (event is VFileContentChangeEvent) {
                                handleFileChange(event.file)
                            }
                        }
                    }
                }
            )
        }

        logger.info("Pi diff watcher started, tracking ${snapshots.size} files")
    }

    fun stopWatching() {
        isWatching = false
        connection?.disconnect()
        connection = null
        snapshots.clear()
    }

    fun snapshotFile(filePath: String) {
        val vFile = LocalFileSystem.getInstance().findFileByPath(filePath) ?: return
        val document = FileDocumentManager.getInstance().getDocument(vFile) ?: return
        snapshots[filePath] = document.text
    }

    fun showDiff(filePath: String) {
        val originalContent = snapshots[filePath] ?: return
        val vFile = LocalFileSystem.getInstance().findFileByPath(filePath) ?: return
        val document = FileDocumentManager.getInstance().getDocument(vFile) ?: return
        val newContent = document.text

        if (originalContent == newContent) return

        val diffContentFactory = DiffContentFactory.getInstance()
        val request = SimpleDiffRequest(
            "Pi Agent: Changes to ${vFile.name}",
            diffContentFactory.create(originalContent),
            diffContentFactory.create(project, document),
            "Before Pi",
            "After Pi"
        )

        DiffManager.getInstance().showDiff(project, request)
    }

    private fun snapshotOpenFiles() {
        val projectPath = project.basePath ?: return
        val fileDocManager = FileDocumentManager.getInstance()
        fileDocManager.unsavedDocuments.forEach { doc ->
            val vFile = fileDocManager.getFile(doc)
            if (vFile != null && vFile.path.startsWith(projectPath)) {
                snapshots[vFile.path] = doc.text
            }
        }
    }

    private fun handleFileChange(file: VirtualFile) {
        val filePath = file.path
        val projectPath = project.basePath ?: return

        if (!filePath.startsWith(projectPath)) return

        if (snapshots.containsKey(filePath)) {
            showDiff(filePath)
        }
    }

    override fun dispose() {
        stopWatching()
    }

    companion object {
        fun getInstance(project: Project): PiDiffWatcher = project.service()
    }
}
