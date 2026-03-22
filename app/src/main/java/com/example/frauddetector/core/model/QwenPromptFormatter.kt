package com.example.frauddetector.core.model

import com.example.frauddetector.core.detection.input.DetectionInput
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QwenPromptFormatter @Inject constructor(
    private val config: LocalModelConfig
) {
    fun format(input: DetectionInput): String {
        return buildString {
            append("<|im_start|>system\n")
            append(config.systemPrompt)
            append("\n<|im_end|>\n")
            append("<|im_start|>user\n")
            append("请基于以下 behavior_seq JSON 判断是否存在风险。")
            append("\nmodel=")
            append(config.displayName)
            append("\njson=\n")
            append(input.json)
            append("\ntext=\n")
            append(input.prompt)
            append("\n<|im_end|>\n")
            append("<|im_start|>assistant\n")
        }
    }
}
