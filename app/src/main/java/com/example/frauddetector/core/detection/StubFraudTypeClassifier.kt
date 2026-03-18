package com.example.frauddetector.core.detection

import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject
import kotlin.math.min

class StubFraudTypeClassifier @Inject constructor() : FraudTypeClassifier {
    override suspend fun classify(sequence: BehaviorSequence): StageAResult {
        val actionTokens = sequence.events.map { it.action.lowercase() }
        val appTokens = sequence.events.mapNotNull { it.app?.lowercase() }
        val topType = when {
            actionTokens.any { "shop" in it || "mall" in it || "购买" in it } ||
                appTokens.any { "闲鱼" in it } -> "fake_shop"

            actionTokens.any { "reward" in it || "task" in it || "刷单" in it } -> "shuadan"

            actionTokens.any { "adult" in it || "porn" in it || "色情" in it } -> "pornographic_inducement"

            else -> "other"
        }

        val confidence = min(0.95, 0.40 + sequence.events.size * 0.05)
        val base = mapOf(
            "fake_shop" to 0.1,
            "shuadan" to 0.1,
            "pornographic_inducement" to 0.1,
            "other" to 0.1
        ).toMutableMap()
        base[topType] = confidence

        return StageAResult(
            topType = topType,
            confidence = confidence,
            distribution = base
        )
    }
}
