package com.example.frauddetector.core.capture

data class CaptureDiagnostics(
    val serviceRunning: Boolean = false,
    val usageAccessGranted: Boolean = false,
    val notificationPermissionGranted: Boolean = true,
    val cameraMonitoringSupported: Boolean = true
)
