package com.example.frauddetector.core.detection

import com.example.frauddetector.core.detection.impl.FallbackFraudDetector
import com.example.frauddetector.core.detection.impl.LocalLlmFraudDetector
import com.example.frauddetector.core.detection.impl.RuleBasedFraudDetector
import com.example.frauddetector.core.detection.input.DetectionInputBuilder
import com.example.frauddetector.core.export.BehaviorSeqAssembler
import com.example.frauddetector.core.export.BehaviorSeqJsonExporter
import com.example.frauddetector.core.inference.LocalInferenceEngine
import com.example.frauddetector.core.model.LocalModelConfig
import com.example.frauddetector.core.model.QwenPromptFormatter
import com.example.frauddetector.core.transform.BehaviorEventLineFormatter
import com.example.frauddetector.core.transform.BehaviorStructProjector
import com.example.frauddetector.core.transform.BehaviorTextProjector
import com.example.frauddetector.domain.model.BehaviorSequence
import com.example.frauddetector.fixtures.behaviorEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectionPipelineTest {

    @Test
    fun detection_input_builder_uses_raw_sequence_independent_of_projection_settings() {
        val builder = DetectionInputBuilder(
            BehaviorSeqAssembler(),
            BehaviorSeqJsonExporter(BehaviorSeqAssembler(), BehaviorStructProjector()),
            BehaviorTextProjector(BehaviorEventLineFormatter())
        )
        val sequence = BehaviorSequence(
            windowStart = 0L,
            windowEnd = 10_000L,
            events = listOf(
                behaviorEvent(1_000L, "文本聊天", observable = false),
                behaviorEvent(2_000L, "打开应用", observable = true)
            )
        )

        val input = builder.build(sequence)

        assertEquals(1, input.events.size)
        assertTrue(input.prompt.contains("打开应用"))
    }

    @Test
    fun fallback_detector_uses_rule_based_detector_when_model_unavailable() {
        val builder = DetectionInputBuilder(
            BehaviorSeqAssembler(),
            BehaviorSeqJsonExporter(BehaviorSeqAssembler(), BehaviorStructProjector()),
            BehaviorTextProjector(BehaviorEventLineFormatter())
        )
        val modelConfig = LocalModelConfig()
        val promptFormatter = QwenPromptFormatter(modelConfig)
        val ruleDetector = RuleBasedFraudDetector(builder)
        val localDetector = LocalLlmFraudDetector(
            builder,
            object : LocalInferenceEngine {
                override fun isModelAvailable(): Boolean = false
                override suspend fun loadModel(modelPath: String): Boolean = false
                override suspend fun runInference(prompt: String): String = ""
                override fun release() = Unit
            },
            modelConfig,
            promptFormatter
        )
        val fallback = FallbackFraudDetector(
            localDetector,
            ruleDetector,
            object : LocalInferenceEngine {
                override fun isModelAvailable(): Boolean = false
                override suspend fun loadModel(modelPath: String): Boolean = false
                override suspend fun runInference(prompt: String): String = ""
                override fun release() = Unit
            }
        )

        val result = kotlinx.coroutines.runBlocking {
            fallback.detect(
                BehaviorSequence(
                    windowStart = 0L,
                    windowEnd = 10_000L,
                    events = listOf(behaviorEvent(1_000L, "打开应用", appType = "金融类app"))
                )
            )
        }

        assertEquals("RULE_BASED", result.source)
    }

    @Test
    fun qwen_prompt_formatter_wraps_json_with_chat_template() {
        val input = DetectionInputBuilder(
            BehaviorSeqAssembler(),
            BehaviorSeqJsonExporter(BehaviorSeqAssembler(), BehaviorStructProjector()),
            BehaviorTextProjector(BehaviorEventLineFormatter())
        ).build(
            BehaviorSequence(
                windowStart = 0L,
                windowEnd = 5_000L,
                events = listOf(behaviorEvent(1_000L, "打开应用", app = "支付宝", appType = "金融类app"))
            )
        )

        val prompt = QwenPromptFormatter(LocalModelConfig()).format(input)

        assertTrue(prompt.contains("<|im_start|>system"))
        assertTrue(prompt.contains("Qwen2-1.5B-Instruct"))
        assertTrue(prompt.contains("behavior_seq"))
    }
}
