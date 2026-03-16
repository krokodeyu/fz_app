package com.example.frauddetector.core.detection

import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject

class StubFraudVerifier @Inject constructor() : FraudVerifier {
    override suspend fun verify(topType: String, sequence: BehaviorSequence): StageBResult {
        val size = sequence.events.size
        val risk = when {
            topType != "other" && size >= 8 -> "HIGH_RISK"
            topType != "other" || size >= 4 -> "SUSPICIOUS"
            else -> "NORMAL"
        }
        val reason = when (risk) {
            "HIGH_RISK" -> "短时间内出现多次可疑行为，且类型偏向 $topType。"
            "SUSPICIOUS" -> "检测到可能与 $topType 相关的行为模式，建议继续观察。"
            else -> "当前行为模式未显示明显诈骗特征。"
        }
        return StageBResult(finalRisk = risk, reason = reason)
    }
}
