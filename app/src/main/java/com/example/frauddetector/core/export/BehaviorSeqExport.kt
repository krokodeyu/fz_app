package com.example.frauddetector.core.export

data class BehaviorSeqExport(
    val behaviorSeq: List<Map<String, Any?>>,
    val score: Int? = null,
    val source: String = "device_capture",
    val caseType: String? = null,
    val meta: Map<String, Any?> = emptyMap()
)
