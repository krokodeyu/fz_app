package com.example.frauddetector.domain.usecase

import com.example.frauddetector.core.export.BehaviorSeqAssembler
import com.example.frauddetector.core.recording.EventRecordingPolicy
import com.example.frauddetector.core.transform.BehaviorStructProjector
import com.example.frauddetector.core.transform.BehaviorTextProjector
import com.example.frauddetector.domain.model.BehaviorSequence
import com.example.frauddetector.domain.model.BehaviorTextProjection
import com.example.frauddetector.domain.model.CollectionSettings
import javax.inject.Inject

class BuildBehaviorTextUseCase @Inject constructor(
    private val behaviorSeqAssembler: BehaviorSeqAssembler,
    private val textProjector: BehaviorTextProjector,
    private val structProjector: BehaviorStructProjector,
    private val recordingPolicy: EventRecordingPolicy
) {
    operator fun invoke(
        sequence: BehaviorSequence,
        settings: CollectionSettings
    ): BehaviorTextProjection {
        val assembledEvents = behaviorSeqAssembler.assemble(sequence)
        val projectedEvents = assembledEvents.filter { recordingPolicy.shouldProjectToText(it, settings) }
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
