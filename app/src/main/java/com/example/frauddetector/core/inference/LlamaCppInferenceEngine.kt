package com.example.frauddetector.core.inference

import android.content.Context
import com.example.frauddetector.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class LlamaCppInferenceEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : LocalInferenceEngine {

    private var loadedModelPath: String? = null

    override fun isModelAvailable(): Boolean {
        val configured = BuildConfig.LOCAL_LLM_MODEL_PATH
        return configured.isNotBlank() && File(configured).exists()
    }

    override suspend fun loadModel(modelPath: String): Boolean {
        val file = File(modelPath)
        val available = file.exists()
        if (available) {
            loadedModelPath = modelPath
        } else {
            Timber.w("llama.cpp model file not found at %s", modelPath)
        }
        return available
    }

    override suspend fun runInference(prompt: String): String {
        val path = loadedModelPath ?: BuildConfig.LOCAL_LLM_MODEL_PATH
        check(path.isNotBlank()) { "No GGUF model loaded. Configure BuildConfig.LOCAL_LLM_MODEL_PATH first." }
        // TODO: Wire real llama.cpp JNI bindings once the merged GGUF model is available.
        return "{\"riskLabel\":\"SUSPICIOUS\",\"fraudSubtype\":null,\"reason\":\"LLM engine placeholder: ${context.packageName}\"}"
    }

    override fun release() {
        loadedModelPath = null
    }
}
