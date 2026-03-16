package com.example.frauddetector.core.detection

data class DetectionResult(
    val stageAType: String,
    val stageAConfidence: Double,
    val finalRisk: String,
    val reason: String
)
