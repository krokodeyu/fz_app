package com.example.frauddetector.domain.usecase

import com.example.frauddetector.data.store.CollectionSettingsStore
import com.example.frauddetector.domain.model.CollectionSettings
import javax.inject.Inject

class UpdateCollectionSettingsUseCase @Inject constructor(
    private val store: CollectionSettingsStore
) {
    suspend fun updateCollectionEnabled(enabled: Boolean) {
        store.update { it.copy(collectionEnabled = enabled) }
    }

    suspend fun updateRecordingEnabled(enabled: Boolean) {
        store.update { it.copy(recordingEnabled = enabled) }
    }

    suspend fun updateObservableOnly(enabled: Boolean) {
        store.update { it.copy(observableOnly = enabled) }
    }

    suspend fun updateTextProjectionEnabled(enabled: Boolean) {
        store.update { it.copy(textProjectionEnabled = enabled) }
    }

    suspend fun updateDetectionEnabled(enabled: Boolean) {
        store.update { it.copy(detectionEnabled = enabled) }
    }

    suspend operator fun invoke(settings: CollectionSettings) {
        store.update { settings }
    }
}
