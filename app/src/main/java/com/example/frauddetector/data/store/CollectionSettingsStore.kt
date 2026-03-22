package com.example.frauddetector.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.frauddetector.domain.model.CollectionSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class CollectionSettingsStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val settings: Flow<CollectionSettings> = dataStore.data.map { preferences ->
        CollectionSettings(
            collectionEnabled = preferences[Keys.collectionEnabled] ?: false,
            recordingEnabled = preferences[Keys.recordingEnabled] ?: true,
            observableOnly = preferences[Keys.observableOnly] ?: true,
            textProjectionEnabled = preferences[Keys.textProjectionEnabled] ?: true,
            detectionEnabled = preferences[Keys.detectionEnabled] ?: true
        )
    }

    suspend fun update(transform: (CollectionSettings) -> CollectionSettings) {
        dataStore.edit { preferences ->
            val current = CollectionSettings(
                collectionEnabled = preferences[Keys.collectionEnabled] ?: false,
                recordingEnabled = preferences[Keys.recordingEnabled] ?: true,
                observableOnly = preferences[Keys.observableOnly] ?: true,
                textProjectionEnabled = preferences[Keys.textProjectionEnabled] ?: true,
                detectionEnabled = preferences[Keys.detectionEnabled] ?: true
            )
            val updated = transform(current)
            preferences[Keys.collectionEnabled] = updated.collectionEnabled
            preferences[Keys.recordingEnabled] = updated.recordingEnabled
            preferences[Keys.observableOnly] = updated.observableOnly
            preferences[Keys.textProjectionEnabled] = updated.textProjectionEnabled
            preferences[Keys.detectionEnabled] = updated.detectionEnabled
        }
    }

    private object Keys {
        val collectionEnabled = booleanPreferencesKey("enable_collect")
        val recordingEnabled = booleanPreferencesKey("enable_record")
        val observableOnly = booleanPreferencesKey("observable_only")
        val textProjectionEnabled = booleanPreferencesKey("enable_text_projection")
        val detectionEnabled = booleanPreferencesKey("enable_detection")
    }
}
