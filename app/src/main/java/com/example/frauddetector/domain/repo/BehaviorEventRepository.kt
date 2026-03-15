package com.example.frauddetector.domain.repo

import com.example.frauddetector.domain.model.BehaviorEvent
import kotlinx.coroutines.flow.Flow

interface BehaviorEventRepository {
    fun observeRecentEvents(limit: Int): Flow<List<BehaviorEvent>>
    suspend fun insertEvent(event: BehaviorEvent)
    suspend fun insertEvents(events: List<BehaviorEvent>)
    suspend fun clearAll()
}
