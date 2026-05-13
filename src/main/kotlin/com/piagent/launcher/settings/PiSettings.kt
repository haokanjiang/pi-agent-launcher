package com.piagent.launcher.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service

/**
 * Persistent settings for Pi Agent plugin.
 */
@Service(Service.Level.APP)
@State(
    name = "PiAgentSettings",
    storages = [Storage("PiAgentSettings.xml")]
)
class PiSettings : PersistentStateComponent<PiSettings.State> {

    data class State(
        var piCommand: String = "pi",
        var model: String = "Default",
        var customModelId: String = "",
        var thinkingLevel: String = "Default",
        var autoOpenFiles: Boolean = true,
        var showNotifications: Boolean = true,
        var shellPath: String = "",
        var extraArgs: String = ""
    )

    private var myState = State()

    override fun getState(): State = myState

    override fun loadState(state: State) {
        myState = state
    }

    companion object {
        fun getInstance(): PiSettings = service()
    }
}
