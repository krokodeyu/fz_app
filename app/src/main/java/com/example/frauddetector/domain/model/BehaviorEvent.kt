package com.example.frauddetector.domain.model

data class BehaviorEvent(
    val timestamp: Long,
    val action: String,
    val app: String?,
    val appType: String?,
    val website: String?,
    val websiteType: String?,
    val information: Map<String, String> = emptyMap(),
    val online: Boolean = true,
    val observable: Boolean = true,
    val source: String,
    val packageName: String? = null
)
