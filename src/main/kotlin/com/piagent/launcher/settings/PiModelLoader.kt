package com.piagent.launcher.settings

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.intellij.openapi.diagnostic.Logger
import java.io.File

/**
 * Loads available models from ~/.pi/agent/models.json
 */
object PiModelLoader {

    private val logger = Logger.getInstance(PiModelLoader::class.java)

    data class ModelInfo(
        val id: String,
        val name: String,
        val provider: String
    ) {
        /** Value used for pi --model flag */
        fun toCommandArg(): String = "$provider/$id"

        /** Display in combo box */
        fun toDisplayString(): String {
            return if (name.isNotBlank()) "$name ($provider/$id)" else "$provider/$id"
        }
    }

    fun loadModels(): List<ModelInfo> {
        val models = mutableListOf<ModelInfo>()
        val modelsFile = File(System.getProperty("user.home"), ".pi/agent/models.json")

        if (!modelsFile.exists()) {
            logger.info("models.json not found at ${modelsFile.path}")
            return models
        }

        try {
            val content = modelsFile.readText()
            val gson = Gson()
            val root = gson.fromJson(content, JsonObject::class.java)
            val providers = root.getAsJsonObject("providers") ?: return models

            for ((providerName, providerElement) in providers.entrySet()) {
                val providerObj = providerElement.asJsonObject
                val modelsArray = providerObj.getAsJsonArray("models") ?: continue

                for (modelElement in modelsArray) {
                    val modelObj = modelElement.asJsonObject
                    val id = modelObj.get("id")?.asString ?: continue
                    val name = modelObj.get("name")?.asString ?: ""

                    models.add(ModelInfo(
                        id = id,
                        name = name,
                        provider = providerName
                    ))
                }
            }
        } catch (e: Exception) {
            logger.warn("Failed to parse models.json", e)
        }

        return models
    }
}
