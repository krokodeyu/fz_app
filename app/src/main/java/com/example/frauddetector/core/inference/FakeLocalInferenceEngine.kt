package com.example.frauddetector.core.inference

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeLocalInferenceEngine @Inject constructor() : LocalInferenceEngine {
    override fun isModelAvailable(): Boolean = false

    override suspend fun loadModel(modelPath: String): Boolean = false

    override suspend fun runInference(prompt: String): String {
        return "{\"riskLabel\":\"SUSPICIOUS\",\"fraudSubtype\":\"rule_like\",\"reason\":\"fake inference\"}"
    }

    override fun release() = Unit
}
