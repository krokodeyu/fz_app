package com.example.frauddetector.core.inference

import android.content.Context
import com.example.frauddetector.core.model.LocalModelConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class LlamaCppInferenceEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localModelConfig: LocalModelConfig
) : LocalInferenceEngine {

    private var loadedModelPath: String? = null

    override fun isModelAvailable(): Boolean {
        val configured = localModelConfig.modelPath
        return configured.isNotBlank() && File(configured).exists()
    }

    override suspend fun loadModel(modelPath: String): Boolean {
        val file = File(modelPath)
        val available = file.exists()
        if (available) {
            loadedModelPath = modelPath
        } else {
            Timber.w("%s model file not found at %s", localModelConfig.displayName, modelPath)
        }
        return available
    }

    override suspend fun runInference(prompt: String): String {
        val path = loadedModelPath ?: localModelConfig.modelPath
        check(path.isNotBlank()) { "No GGUF model loaded. Configure BuildConfig.LOCAL_LLM_MODEL_PATH first." }
        // TODO: Wire real llama.cpp JNI bindings for Qwen2-1.5B-Instruct GGUF execution.
        return "{\"riskLabel\":\"SUSPICIOUS\",\"fraudSubtype\":null,\"reason\":\"Qwen placeholder via llama.cpp: ${context.packageName}\"}"
    }

    override fun release() {
        loadedModelPath = null
    }
}
