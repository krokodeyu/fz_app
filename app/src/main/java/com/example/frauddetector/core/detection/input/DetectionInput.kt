package com.example.frauddetector.core.detection.input

import com.example.frauddetector.domain.model.BehaviorEvent

data class DetectionInput(
    val events: List<BehaviorEvent>,
    val prompt: String,
    val json: String,
    val meta: Map<String, String>
)
