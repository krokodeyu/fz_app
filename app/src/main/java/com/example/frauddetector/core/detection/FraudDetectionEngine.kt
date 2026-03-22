package com.example.frauddetector.core.detection

import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject

/**
 * Demo-only two-stage engine retained for backwards compatibility with earlier experiments.
 * Production detection goes through [FraudDetector].
 */
class FraudDetectionEngine @Inject constructor(
    private val classifier: FraudTypeClassifier,
    private val verifier: FraudVerifier
) {
    suspend fun detect(sequence: BehaviorSequence): DetectionResult {
        val stageA = classifier.classify(sequence)
        val stageB = verifier.verify(stageA.topType, sequence)
        return DetectionResult(
            riskLabel = stageB.riskLabel,
            fraudSubtype = stageA.topType,
            source = "DEMO_STUB",
            reason = stageB.reason,
            evidence = stageA.typeScores.entries.map { "${it.key}=${it.value}" },
            debug = mapOf("confidence" to stageA.confidence.toString())
        )
    }
}
