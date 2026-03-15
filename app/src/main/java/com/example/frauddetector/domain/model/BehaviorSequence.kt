package com.example.frauddetector.domain.model

data class BehaviorSequence(
    val windowStart: Long,
    val windowEnd: Long,
    val events: List<BehaviorEvent>
)
