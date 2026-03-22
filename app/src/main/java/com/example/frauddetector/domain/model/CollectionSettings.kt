package com.example.frauddetector.domain.model

data class CollectionSettings(
    val collectionEnabled: Boolean = false,
    val recordingEnabled: Boolean = true,
    val observableOnly: Boolean = true,
    val textProjectionEnabled: Boolean = true,
    val detectionEnabled: Boolean = true
)
