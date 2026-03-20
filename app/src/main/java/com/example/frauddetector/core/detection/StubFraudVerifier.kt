package com.example.frauddetector.core.detection

import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject

/** Demo-only verifier. Keep out of the production detector selection path. */
class StubFraudVerifier @Inject constructor() : FraudVerifier {
    override suspend fun verify(topType: String, sequence: BehaviorSequence): StageBResult {
        val riskLabel = when {
            topType != "other" && sequence.events.size >= 6 -> "HIGH_RISK"
            topType != "other" -> "SUSPICIOUS"
            else -> "NORMAL"
        }
        return StageBResult(
            riskLabel = riskLabel,
            reason = when (riskLabel) {
                "HIGH_RISK" -> "Demo detector observed a dense high-risk pattern."
                "SUSPICIOUS" -> "Demo detector observed a suspicious observable sequence."
                else -> "Demo detector did not find a risky observable sequence."
            }
        )
    }
}
