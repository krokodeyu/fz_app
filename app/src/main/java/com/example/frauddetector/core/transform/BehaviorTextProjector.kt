package com.example.frauddetector.core.transform

import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorTextProjector @Inject constructor(
    private val lineFormatter: BehaviorEventLineFormatter
) {

    fun project(sequence: BehaviorSequence): String = project(sequence.events)

    fun project(events: List<BehaviorEvent>): String {
        if (events.isEmpty()) return ""
        return events.joinToString(separator = "\n") { lineFormatter.format(it) }
    }
}
