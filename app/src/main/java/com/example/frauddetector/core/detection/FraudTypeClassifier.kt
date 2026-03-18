package com.example.frauddetector.core.detection

import com.example.frauddetector.domain.model.BehaviorSequence

interface FraudTypeClassifier {
    suspend fun classify(sequence: BehaviorSequence): StageAResult
}

data class StageAResult(
    val topType: String,
    val confidence: Double,
    val distribution: Map<String, Double>
)
