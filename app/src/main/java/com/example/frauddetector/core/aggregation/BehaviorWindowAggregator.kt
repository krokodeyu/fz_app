package com.example.frauddetector.core.aggregation

import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorWindowAggregator @Inject constructor() {

    fun aggregate(events: List<BehaviorEvent>, windowMillis: Long): BehaviorSequence {
        val now = System.currentTimeMillis()
        val start = now - windowMillis
        val inWindow = events.filter { it.timestamp in start..now }.sortedBy { it.timestamp }
        return BehaviorSequence(
            windowStart = start,
            windowEnd = now,
            events = inWindow
        )
    }
}
