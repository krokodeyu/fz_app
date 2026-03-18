package com.example.frauddetector.core.detection

import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject

class FraudDetectionEngine @Inject constructor(
    private val classifier: FraudTypeClassifier,
    private val verifier: FraudVerifier
) {
    suspend fun detect(sequence: BehaviorSequence): DetectionResult {
        val stageA = classifier.classify(sequence)
        val stageB = verifier.verify(stageA.topType, sequence)
        return DetectionResult(
            stageAType = stageA.topType,
            stageAConfidence = stageA.confidence,
            finalRisk = stageB.finalRisk,
            reason = stageB.reason
        )
    }
}
