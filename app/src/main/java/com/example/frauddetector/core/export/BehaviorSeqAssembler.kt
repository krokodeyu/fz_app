package com.example.frauddetector.core.export

import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorSeqAssembler @Inject constructor() {

    fun assemble(sequence: BehaviorSequence, dedupeWindowMillis: Long = 3_000L): List<BehaviorEvent> {
        val sorted = sequence.events.sortedBy { it.timestamp }
        if (sorted.isEmpty()) return emptyList()
        val result = mutableListOf<BehaviorEvent>()
        sorted.forEach { event ->
            val previous = result.lastOrNull()
            val isDuplicate = previous != null &&
                previous.action == event.action &&
                previous.packageName == event.packageName &&
                previous.website == event.website &&
                event.timestamp - previous.timestamp <= dedupeWindowMillis
            if (!isDuplicate) {
                result += event
            }
        }
        return result
    }
}
