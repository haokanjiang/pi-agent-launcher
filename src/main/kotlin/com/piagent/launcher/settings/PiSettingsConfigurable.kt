package com.piagent.launcher.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import javax.swing.*

/**
 * Settings UI: Settings → Tools → Pi Agent
 */
class PiSettingsConfigurable : Configurable {

    private var panel: JPanel? = null
    private var piCommandField: JBTextField? = null
    private var modelCombo: ComboBox<String>? = null
    private var customModelField: JBTextField? = null
    private var thinkingLevelCombo: ComboBox<String>? = null
    private var extraArgsField: JBTextField? = null
    private var autoOpenFilesCheckbox: JCheckBox? = null
    private var showNotificationsCheckbox: JCheckBox? = null

    companion object {
        val THINKING_LEVELS = arrayOf(
            "Default",
            "none",
            "low",
            "medium",
            "high",
            "max"
        )

        fun loadModelOptions(): Array<String> {
            val models = PiModelLoader.loadModels()
            val options = mutableListOf("Default")
            models.forEach { options.add("${it.provider}/${it.id}") }
            return options.toTypedArray()
        }
    }

    override fun getDisplayName(): String = "Pi Agent"

    override fun createComponent(): JComponent {
        val settings = PiSettings.getInstance().state

        piCommandField = JBTextField(settings.piCommand)
        val modelOptions = loadModelOptions()
        modelCombo = ComboBox(modelOptions).apply {
            selectedItem = if (settings.model in modelOptions) settings.model else "Default"
            preferredSize = java.awt.Dimension(400, preferredSize.height)
        }
        customModelField = JBTextField(settings.customModelId).apply {
            emptyText.text = "e.g. claude-sonnet-4-20250514"
            preferredSize = java.awt.Dimension(400, preferredSize.height)
        }
        thinkingLevelCombo = ComboBox(THINKING_LEVELS).apply {
            selectedItem = if (settings.thinkingLevel in THINKING_LEVELS) settings.thinkingLevel else "Default"
            preferredSize = java.awt.Dimension(400, preferredSize.height)
        }
        extraArgsField = JBTextField(settings.extraArgs)
        autoOpenFilesCheckbox = JCheckBox("Auto-open files modified by Pi", settings.autoOpenFiles)
        showNotificationsCheckbox = JCheckBox("Show notification when Pi finishes", settings.showNotifications)

        panel = FormBuilder.createFormBuilder()
            // Model section
            .addSeparator()
            .addComponent(JBLabel("Model").apply {
                font = font.deriveFont(java.awt.Font.BOLD)
                border = JBUI.Borders.emptyTop(4)
            })
            .addLabeledComponent(JBLabel("Model:"), modelCombo!!, 1, false)
            .addLabeledComponent(JBLabel("Custom model id:"), customModelField!!, 1, false)
            .addComponentToRightColumn(JBLabel("If set, overrides the model dropdown.").apply {
                foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                font = JBUI.Fonts.smallFont()
            }, 0)
            .addLabeledComponent(JBLabel("Thinking level:"), thinkingLevelCombo!!, 1, false)
            .addComponentToRightColumn(JBLabel("Some models may not support all thinking levels.").apply {
                foreground = JBUI.CurrentTheme.ContextHelp.FOREGROUND
                font = JBUI.Fonts.smallFont()
            }, 0)

            // General section
            .addSeparator()
            .addComponent(JBLabel("General").apply {
                font = font.deriveFont(java.awt.Font.BOLD)
                border = JBUI.Borders.emptyTop(4)
            })
            .addLabeledComponent(JBLabel("Pi command:"), piCommandField!!, 1, false)
            .addLabeledComponent(JBLabel("Extra arguments:"), extraArgsField!!, 1, false)

            // Options section
            .addSeparator()
            .addComponent(JBLabel("Options").apply {
                font = font.deriveFont(java.awt.Font.BOLD)
                border = JBUI.Borders.emptyTop(4)
            })
            .addComponent(autoOpenFilesCheckbox!!, 1)
            .addComponent(showNotificationsCheckbox!!, 1)

            .addComponentFillVertically(JPanel(), 0)
            .panel

        return panel!!
    }

    override fun isModified(): Boolean {
        val settings = PiSettings.getInstance().state
        return piCommandField?.text != settings.piCommand ||
                modelCombo?.selectedItem != settings.model ||
                customModelField?.text != settings.customModelId ||
                thinkingLevelCombo?.selectedItem != settings.thinkingLevel ||
                extraArgsField?.text != settings.extraArgs ||
                autoOpenFilesCheckbox?.isSelected != settings.autoOpenFiles ||
                showNotificationsCheckbox?.isSelected != settings.showNotifications
    }

    override fun apply() {
        val settings = PiSettings.getInstance()
        settings.loadState(
            PiSettings.State(
                piCommand = piCommandField?.text ?: "pi",
                model = modelCombo?.selectedItem as? String ?: "Default",
                customModelId = customModelField?.text ?: "",
                thinkingLevel = thinkingLevelCombo?.selectedItem as? String ?: "Default",
                extraArgs = extraArgsField?.text ?: "",
                autoOpenFiles = autoOpenFilesCheckbox?.isSelected ?: true,
                showNotifications = showNotificationsCheckbox?.isSelected ?: true
            )
        )
    }

    override fun reset() {
        val settings = PiSettings.getInstance().state
        piCommandField?.text = settings.piCommand
        modelCombo?.selectedItem = settings.model
        customModelField?.text = settings.customModelId
        thinkingLevelCombo?.selectedItem = settings.thinkingLevel
        extraArgsField?.text = settings.extraArgs
        autoOpenFilesCheckbox?.isSelected = settings.autoOpenFiles
        showNotificationsCheckbox?.isSelected = settings.showNotifications
    }

    override fun disposeUIResources() {
        panel = null
    }
}
