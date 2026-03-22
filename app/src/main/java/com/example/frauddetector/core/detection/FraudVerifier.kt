package com.example.frauddetector.core.detection

import com.example.frauddetector.domain.model.BehaviorSequence

/** Demo-only verifier kept for debug fallback comparison. */
interface FraudVerifier {
    suspend fun verify(topType: String, sequence: BehaviorSequence): StageBResult
}

data class StageBResult(
    val riskLabel: String,
    val reason: String
)
