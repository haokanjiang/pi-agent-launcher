package com.piagent.launcher.settings

import com.piagent.launcher.domain.PiModel
import com.piagent.launcher.infrastructure.FilePiModelRepository
import com.piagent.launcher.infrastructure.PiModelRepository

/**
 * Supplies the model list to the settings UI.
 *
 * The repository is swappable so the form can be built in tests without touching
 * the user's home directory.
 */
internal object SettingsModelCatalog {

    @Volatile
    var repository: PiModelRepository = FilePiModelRepository()

    fun models(): List<PiModel> = repository.loadModels()
}
