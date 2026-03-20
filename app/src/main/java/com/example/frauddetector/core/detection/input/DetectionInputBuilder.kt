package com.example.frauddetector.core.detection.input

import com.example.frauddetector.core.export.BehaviorSeqAssembler
import com.example.frauddetector.core.export.BehaviorSeqJsonExporter
import com.example.frauddetector.core.transform.BehaviorTextProjector
import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectionInputBuilder @Inject constructor(
    private val behaviorSeqAssembler: BehaviorSeqAssembler,
    private val jsonExporter: BehaviorSeqJsonExporter,
    private val textProjector: BehaviorTextProjector
) {
    fun build(sequence: BehaviorSequence): DetectionInput {
        val events = behaviorSeqAssembler.assemble(sequence).filter { it.observable }
        val normalizedSequence = sequence.copy(events = events)
        val export = jsonExporter.export(normalizedSequence)
        return DetectionInput(
            events = events,
            prompt = textProjector.project(events),
            json = jsonExporter.toJson(export),
            meta = export.meta.mapValues { it.value.toString() }
        )
    }
}
