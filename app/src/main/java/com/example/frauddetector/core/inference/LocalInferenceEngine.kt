package com.example.frauddetector.core.inference

interface LocalInferenceEngine {
    fun isModelAvailable(): Boolean
    suspend fun loadModel(modelPath: String): Boolean
    suspend fun runInference(prompt: String): String
    fun release()
}
