package com.example.frauddetector.domain.usecase

import com.example.frauddetector.core.aggregation.BehaviorWindowAggregator
import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject

class AggregateWindowUseCase @Inject constructor(
    private val aggregator: BehaviorWindowAggregator
) {
    operator fun invoke(events: List<BehaviorEvent>, windowMillis: Long): BehaviorSequence {
        return aggregator.aggregate(events, windowMillis)
    }
}
