package com.example.frauddetector.core.detection

data class DetectionResult(
    val riskLabel: String,
    val fraudSubtype: String? = null,
    val source: String,
    val reason: String,
    val evidence: List<String> = emptyList(),
    val debug: Map<String, String> = emptyMap()
)
