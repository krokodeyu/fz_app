package com.example.frauddetector.domain.usecase

import com.example.frauddetector.core.recording.EventRecordingPolicy
import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.model.CollectionSettings
import com.example.frauddetector.domain.repo.BehaviorEventRepository
import javax.inject.Inject

class RecordBehaviorEventUseCase @Inject constructor(
    private val repository: BehaviorEventRepository,
    private val recordingPolicy: EventRecordingPolicy
) {
    suspend operator fun invoke(event: BehaviorEvent, settings: CollectionSettings): Boolean {
        if (!recordingPolicy.shouldRecord(event, settings)) return false
        repository.insertEvent(event)
        return true
    }
}
