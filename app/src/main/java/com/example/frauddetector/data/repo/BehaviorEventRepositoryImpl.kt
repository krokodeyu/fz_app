package com.example.frauddetector.data.repo

import com.example.frauddetector.data.db.BehaviorEventDao
import com.example.frauddetector.data.mappers.toDomain
import com.example.frauddetector.data.mappers.toEntity
import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.repo.BehaviorEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BehaviorEventRepositoryImpl @Inject constructor(
    private val dao: BehaviorEventDao
) : BehaviorEventRepository {

    override fun observeRecentEvents(limit: Int): Flow<List<BehaviorEvent>> {
        return dao.observeRecent(limit).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun insertEvent(event: BehaviorEvent) {
        dao.insert(event.toEntity())
    }

    override suspend fun insertEvents(events: List<BehaviorEvent>) {
        dao.insertAll(events.map { it.toEntity() })
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}
