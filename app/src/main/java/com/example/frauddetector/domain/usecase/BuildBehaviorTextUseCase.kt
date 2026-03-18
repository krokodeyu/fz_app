package com.example.frauddetector.domain.usecase

import com.example.frauddetector.core.recording.EventRecordingPolicy
import com.example.frauddetector.core.recording.ObservableEventFilter
import com.example.frauddetector.core.transform.BehaviorStructProjector
import com.example.frauddetector.core.transform.BehaviorTextProjector
import com.example.frauddetector.domain.model.BehaviorSequence
import com.example.frauddetector.domain.model.BehaviorTextProjection
import com.example.frauddetector.domain.model.CollectionSettings
import javax.inject.Inject

class BuildBehaviorTextUseCase @Inject constructor(
    private val observableEventFilter: ObservableEventFilter,
    private val textProjector: BehaviorTextProjector,
    private val structProjector: BehaviorStructProjector,
    private val recordingPolicy: EventRecordingPolicy
) {
    operator fun invoke(
        sequence: BehaviorSequence,
        settings: CollectionSettings
    ): BehaviorTextProjection {
        val observableEvents = observableEventFilter.filter(sequence.events, settings.observableOnly)
        val projectedEvents = observableEvents.filter { recordingPolicy.shouldProjectToText(it, settings) }
        if (!settings.textProjectionEnabled) {
            return BehaviorTextProjection(projectedEvents = projectedEvents)
        }
        return BehaviorTextProjection(
            projectedEvents = projectedEvents,
            text = textProjector.project(projectedEvents),
            struct = structProjector.project(projectedEvents)
        )
    }
}
