package com.example.frauddetector.core.detection

import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject
import kotlin.math.min

/** Demo-only classifier. Keep out of the production detector selection path. */
class StubFraudTypeClassifier @Inject constructor() : FraudTypeClassifier {
    override suspend fun classify(sequence: BehaviorSequence): StageAResult {
        val financeEvents = sequence.events.count { it.appType == "金融类app" }
        val ecommerceEvents = sequence.events.count { it.appType == "电商类app" }
        val topType = when {
            ecommerceEvents > 0 && financeEvents > 0 -> "transaction_flow_risk"
            financeEvents > 2 -> "payment_flow_risk"
            else -> "other"
        }
        val confidence = min(0.9, 0.35 + sequence.events.size * 0.04)
        return StageAResult(
            topType = topType,
            confidence = confidence,
            typeScores = mapOf(
                "transaction_flow_risk" to if (topType == "transaction_flow_risk") confidence else 0.15,
                "payment_flow_risk" to if (topType == "payment_flow_risk") confidence else 0.15,
                "other" to if (topType == "other") confidence else 0.15
            )
        )
    }
}
