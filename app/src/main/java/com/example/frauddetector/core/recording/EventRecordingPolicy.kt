package com.example.frauddetector.core.recording

import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.model.CollectionSettings

interface EventRecordingPolicy {
    fun shouldRecord(event: BehaviorEvent, settings: CollectionSettings): Boolean
    fun shouldProjectToText(event: BehaviorEvent, settings: CollectionSettings): Boolean
    fun shouldUseForDetection(event: BehaviorEvent, settings: CollectionSettings): Boolean
}
