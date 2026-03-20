package com.example.frauddetector.core.detection

import com.example.frauddetector.domain.model.BehaviorSequence

/** Demo-only Stage A classifier kept for comparison/debug. Not used in the production detection path. */
interface FraudTypeClassifier {
    suspend fun classify(sequence: BehaviorSequence): StageAResult
}

data class StageAResult(
    val topType: String,
    val confidence: Double,
    val typeScores: Map<String, Double>
)
