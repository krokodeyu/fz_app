package com.example.frauddetector.core.capture

import kotlinx.coroutines.flow.StateFlow

interface CollectionRuntimeController {
    val serviceRunning: StateFlow<Boolean>
    val diagnostics: StateFlow<CaptureDiagnostics>
    fun syncCollectionEnabled(enabled: Boolean)
    fun refreshDiagnostics()
}
