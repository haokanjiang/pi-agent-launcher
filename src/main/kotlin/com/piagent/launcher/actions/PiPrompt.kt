package com.piagent.launcher.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFileSystemItem
import com.piagent.launcher.domain.PiFileReference
import com.piagent.launcher.services.PiSessionService

/**
 * Shared behaviour for "send something to Pi" actions: resolve the session,
 * start it if needed, insert the reference and focus the terminal.
 */
internal object PiPrompt {

    /**
     * Insert [references] into Pi's prompt, launching Pi first when needed.
     * A trailing space is added so the user can keep typing immediately.
     */
    fun insertReferences(project: Project, references: List<PiFileReference>) {
        if (references.isEmpty()) return

        val text = references.joinToString(" ") { it.render() }
        if (text.isBlank()) return

        val session = PiSessionService.getInstance(project)
        if (!session.isRunning()) {
            session.launch()
        }
        session.insertText("$text ")
        session.focus()
    }
}

/**
 * Resolves the files an action was invoked on, tolerating the several shapes the
 * platform delivers them in (editor, project view, PSI).
 */
internal object EventFiles {

    fun from(e: AnActionEvent): List<VirtualFile> {
        e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.takeIf { it.isNotEmpty() }?.let { return it.toList() }

        e.getData(CommonDataKeys.VIRTUAL_FILE)?.let { return listOf(it) }

        e.getData(LangDataKeys.PSI_ELEMENT_ARRAY)
            ?.mapNotNull { (it as? PsiFileSystemItem)?.virtualFile }
            ?.takeIf { it.isNotEmpty() }
            ?.let { return it }

        e.getData(CommonDataKeys.PSI_FILE)?.virtualFile?.let { return listOf(it) }

        return emptyList()
    }

    fun selectable(e: AnActionEvent): List<VirtualFile> = from(e).filterNot { it.isDirectory }
}
