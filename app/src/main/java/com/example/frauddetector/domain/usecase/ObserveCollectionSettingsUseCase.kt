package com.example.frauddetector.domain.usecase

import com.example.frauddetector.data.store.CollectionSettingsStore
import com.example.frauddetector.domain.model.CollectionSettings
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveCollectionSettingsUseCase @Inject constructor(
    private val store: CollectionSettingsStore
) {
    operator fun invoke(): Flow<CollectionSettings> = store.settings
}
