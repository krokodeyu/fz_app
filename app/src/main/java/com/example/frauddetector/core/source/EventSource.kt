package com.example.frauddetector.core.source

import com.example.frauddetector.domain.model.BehaviorEvent
import kotlinx.coroutines.flow.Flow

interface EventSource {
    val events: Flow<BehaviorEvent>
    fun start()
    fun stop()
}
