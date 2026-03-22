package com.example.frauddetector.core.capture

import kotlinx.coroutines.flow.StateFlow

interface CollectionRuntimeController {
    val serviceRunning: StateFlow<Boolean>
    fun syncCollectionEnabled(enabled: Boolean)
}
