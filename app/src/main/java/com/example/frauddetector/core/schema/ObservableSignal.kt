package com.example.frauddetector.core.schema

data class ObservableSignal(
    val action: StandardBehaviorAction,
    val timestamp: Long,
    val packageName: String? = null,
    val website: String? = null,
    val websiteType: String? = null,
    val information: Map<String, String> = emptyMap(),
    val online: Boolean = true,
    val observable: Boolean = action.observable,
    val source: String,
    val previousPackageName: String? = null
)
