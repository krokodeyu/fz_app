package com.example.frauddetector.core.model

import com.example.frauddetector.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalModelConfig @Inject constructor() {
    val modelId: String = BuildConfig.LOCAL_LLM_MODEL_ID
    val modelPath: String = BuildConfig.LOCAL_LLM_MODEL_PATH
    val displayName: String = "Qwen2-1.5B-Instruct"
    val preferredRuntime: String = "llama.cpp / GGUF"
    val systemPrompt: String = """
        你是一个 Android 端侧反诈检测器。你只能依据可观测行为序列输出 JSON，禁止编造不可观测事实。
        输出格式必须是：
        {"riskLabel":"NORMAL|SUSPICIOUS|HIGH_RISK","fraudSubtype":string|null,"reason":string}
    """.trimIndent()
}
