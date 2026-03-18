package com.example.frauddetector.core.detection

import com.example.frauddetector.domain.model.BehaviorSequence

interface FraudVerifier {
    suspend fun verify(topType: String, sequence: BehaviorSequence): StageBResult
}

data class StageBResult(
    val finalRisk: String,
    val reason: String
)
