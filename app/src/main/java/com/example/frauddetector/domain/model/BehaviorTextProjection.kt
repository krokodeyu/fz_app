package com.example.frauddetector.domain.model

data class BehaviorTextProjection(
    val projectedEvents: List<BehaviorEvent> = emptyList(),
    val text: String = "",
    val struct: List<Map<String, Any?>> = emptyList()
)
