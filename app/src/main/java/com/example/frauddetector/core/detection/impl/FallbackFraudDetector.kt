package com.example.frauddetector.core.detection.impl

import com.example.frauddetector.core.detection.DetectionResult
import com.example.frauddetector.core.detection.FraudDetector
import com.example.frauddetector.core.inference.LocalInferenceEngine
import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class FallbackFraudDetector @Inject constructor(
    private val localLlmFraudDetector: LocalLlmFraudDetector,
    private val ruleBasedFraudDetector: RuleBasedFraudDetector,
    private val localInferenceEngine: LocalInferenceEngine
) : FraudDetector {

    override suspend fun detect(sequence: BehaviorSequence): DetectionResult {
        return if (localInferenceEngine.isModelAvailable()) {
            runCatching { localLlmFraudDetector.detect(sequence) }
                .onFailure { Timber.w(it, "Local LLM detector failed; fallback to rules") }
                .getOrElse { ruleBasedFraudDetector.detect(sequence) }
        } else {
            ruleBasedFraudDetector.detect(sequence)
        }
    }
}
