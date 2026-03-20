package com.example.frauddetector.core.detection.impl

import com.example.frauddetector.BuildConfig
import com.example.frauddetector.core.detection.DetectionResult
import com.example.frauddetector.core.detection.FraudDetector
import com.example.frauddetector.core.detection.input.DetectionInputBuilder
import com.example.frauddetector.core.inference.LocalInferenceEngine
import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalLlmFraudDetector @Inject constructor(
    private val detectionInputBuilder: DetectionInputBuilder,
    private val inferenceEngine: LocalInferenceEngine
) : FraudDetector {

    override suspend fun detect(sequence: BehaviorSequence): DetectionResult {
        val input = detectionInputBuilder.build(sequence)
        val available = inferenceEngine.isModelAvailable() &&
            inferenceEngine.loadModel(BuildConfig.LOCAL_LLM_MODEL_PATH)
        require(available) { "Local GGUF model unavailable" }
        val raw = inferenceEngine.runInference(input.prompt)
        return DetectionResult(
            riskLabel = if (raw.contains("HIGH_RISK")) "HIGH_RISK" else if (raw.contains("SUSPICIOUS")) "SUSPICIOUS" else "NORMAL",
            fraudSubtype = Regex("\"fraudSubtype\":\"(.*?)\"").find(raw)?.groupValues?.getOrNull(1),
            source = "LOCAL_LLM",
            reason = raw,
            evidence = listOf(input.prompt.take(120)),
            debug = input.meta + mapOf("engine" to inferenceEngine::class.java.simpleName)
        )
    }
}
