package com.example.frauddetector.domain.usecase

import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.repo.BehaviorEventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRecentEventsUseCase @Inject constructor(
    private val repository: BehaviorEventRepository
) {
    operator fun invoke(limit: Int): Flow<List<BehaviorEvent>> = repository.observeRecentEvents(limit)
}
