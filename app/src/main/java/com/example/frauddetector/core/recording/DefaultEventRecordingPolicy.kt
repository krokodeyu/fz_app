package com.example.frauddetector.core.recording

import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.model.CollectionSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultEventRecordingPolicy @Inject constructor(
    private val observableEventFilter: ObservableEventFilter
) : EventRecordingPolicy {

    override fun shouldRecord(event: BehaviorEvent, settings: CollectionSettings): Boolean {
        if (!settings.collectionEnabled || !settings.recordingEnabled) return false
        return !settings.observableOnly || observableEventFilter.isObservable(event)
    }

    override fun shouldProjectToText(event: BehaviorEvent, settings: CollectionSettings): Boolean {
        return !settings.observableOnly || observableEventFilter.isObservable(event)
    }

    override fun shouldUseForDetection(event: BehaviorEvent, settings: CollectionSettings): Boolean {
        if (!settings.detectionEnabled) return false
        return shouldProjectToText(event, settings)
    }
}
