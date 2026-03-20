package com.example.frauddetector.core.detection.impl

import com.example.frauddetector.core.detection.DetectionResult
import com.example.frauddetector.core.detection.FraudDetector
import com.example.frauddetector.core.detection.input.DetectionInputBuilder
import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RuleBasedFraudDetector @Inject constructor(
    private val detectionInputBuilder: DetectionInputBuilder
) : FraudDetector {

    override suspend fun detect(sequence: BehaviorSequence): DetectionResult {
        val input = detectionInputBuilder.build(sequence)
        val financeOps = input.events.count { it.appType == "金融类app" }
        val ecommerceOps = input.events.count { it.appType == "电商类app" }
        val cameraOps = input.events.count { it.action == "调用相机" || it.action == "打开相机" }
        val transitionRisk = if (financeOps >= 2 && ecommerceOps >= 1) 1 else 0
        val score = financeOps + ecommerceOps + cameraOps + transitionRisk
        val riskLabel = when {
            score >= 5 -> "HIGH_RISK"
            score >= 3 -> "SUSPICIOUS"
            else -> "NORMAL"
        }
        val subtype = when {
            ecommerceOps > 0 && financeOps > 0 -> "transaction_flow_risk"
            cameraOps > 0 -> "camera_triggered_flow"
            else -> null
        }
        return DetectionResult(
            riskLabel = riskLabel,
            fraudSubtype = subtype,
            source = "RULE_BASED",
            reason = when (riskLabel) {
                "HIGH_RISK" -> "检测到跨电商/金融/相机的高风险可观测行为链。"
                "SUSPICIOUS" -> "检测到多次可观测高风险行为切换，建议人工复核。"
                else -> "当前仅观察到常规可观测事件。"
            },
            evidence = buildList {
                add("finance_ops=$financeOps")
                add("ecommerce_ops=$ecommerceOps")
                add("camera_ops=$cameraOps")
            },
            debug = input.meta + mapOf("input_event_count" to input.events.size.toString())
        )
    }
}
