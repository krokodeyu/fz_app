package com.example.frauddetector.domain.model

data class BehaviorEvent(
    val timestamp: Long,
    val action: String,
    val app: String?,
    val appType: String?,
    val website: String?,
    val websiteType: String?,
    val source: String
)
