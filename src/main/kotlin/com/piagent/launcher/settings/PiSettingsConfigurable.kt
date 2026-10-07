package com.piagent.launcher.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * Settings → Tools → Pi Agent.
 *
 * Instantiated reflectively by the platform, so it must keep a no-argument
 * constructor; the form itself is delegated to [PiSettingsForm].
 */
class PiSettingsConfigurable : Configurable {

    private var form: PiSettingsForm? = null

    override fun getDisplayName(): String = "Pi Agent"

    override fun createComponent(): JComponent {
        val settings = PiSettings.getInstance()
        return PiSettingsForm(settings.state, PiSettingsForm.loadModelOptions())
            .also { form = it }
            .panel
    }

    override fun isModified(): Boolean {
        val settings = PiSettings.getInstance().state
        return form?.toState()?.let { it != settings } ?: false
    }

    override fun apply() {
        form?.toState()?.let { PiSettings.getInstance().loadState(it) }
    }

    override fun reset() {
        form?.populate(PiSettings.getInstance().state)
    }

    override fun disposeUIResources() {
        form = null
    }
}
