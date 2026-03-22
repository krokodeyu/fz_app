package com.example.frauddetector.fixtures

import com.example.frauddetector.core.detection.DetectionResult
import com.example.frauddetector.core.detection.FraudDetector
import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.model.BehaviorSequence
import com.example.frauddetector.domain.repo.BehaviorEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicInteger

fun behaviorEvent(
    timestamp: Long,
    action: String,
    app: String? = null,
    appType: String? = null,
    source: String = "test",
    packageName: String? = null,
    observable: Boolean = true,
    website: String? = null,
    websiteType: String? = null,
    information: Map<String, String> = emptyMap(),
    online: Boolean = true
): BehaviorEvent = BehaviorEvent(
    timestamp = timestamp,
    action = action,
    app = app,
    appType = appType,
    website = website,
    websiteType = websiteType,
    information = information,
    online = online,
    observable = observable,
    source = source,
    packageName = packageName
)

class CountingBehaviorEventRepository(
    private val events: List<BehaviorEvent>
) : BehaviorEventRepository {
    val subscriptions = AtomicInteger(0)

    override fun observeRecentEvents(limit: Int): Flow<List<BehaviorEvent>> = flow {
        subscriptions.incrementAndGet()
        emit(events.take(limit))
    }

    override suspend fun insertEvent(event: BehaviorEvent) = Unit
    override suspend fun insertEvents(events: List<BehaviorEvent>) = Unit
    override suspend fun clearAll() = Unit
}

class InMemoryBehaviorEventRepository : BehaviorEventRepository {
    private val values = mutableListOf<BehaviorEvent>()
    private val flow = MutableSharedFlow<List<BehaviorEvent>>(replay = 1)

    init {
        flow.tryEmit(emptyList())
    }

    override fun observeRecentEvents(limit: Int): Flow<List<BehaviorEvent>> = flow

    override suspend fun insertEvent(event: BehaviorEvent) {
        values += event
        flow.emit(values.sortedByDescending { it.timestamp }.take(30))
    }

    override suspend fun insertEvents(events: List<BehaviorEvent>) {
        values += events
        flow.emit(values.sortedByDescending { it.timestamp }.take(30))
    }

    override suspend fun clearAll() {
        values.clear()
        flow.emit(emptyList())
    }
}



class CapturingBehaviorEventRepository : BehaviorEventRepository {
    val inserted = mutableListOf<BehaviorEvent>()

    override fun observeRecentEvents(limit: Int): Flow<List<BehaviorEvent>> = flow {
        emit(inserted.takeLast(limit).reversed())
    }

    override suspend fun insertEvent(event: BehaviorEvent) {
        inserted += event
    }

    override suspend fun insertEvents(events: List<BehaviorEvent>) {
        inserted += events
    }

    override suspend fun clearAll() {
        inserted.clear()
    }
}

class FakeFraudDetector : FraudDetector {
    override suspend fun detect(sequence: BehaviorSequence): DetectionResult {
        return DetectionResult(
            riskLabel = if (sequence.events.isEmpty()) "NORMAL" else "SUSPICIOUS",
            source = "TEST",
            reason = "test detector"
        )
    }
}


class FakeCollectionRuntimeController(initialRunning: Boolean = false) : com.example.frauddetector.core.capture.CollectionRuntimeController {
    private val state = kotlinx.coroutines.flow.MutableStateFlow(initialRunning)
    private val diagnosticsState = kotlinx.coroutines.flow.MutableStateFlow(
        com.example.frauddetector.core.capture.CaptureDiagnostics(
            serviceRunning = initialRunning,
            usageAccessGranted = true,
            notificationPermissionGranted = true,
            cameraMonitoringSupported = true
        )
    )
    override val serviceRunning: kotlinx.coroutines.flow.StateFlow<Boolean> = state
    override val diagnostics: kotlinx.coroutines.flow.StateFlow<com.example.frauddetector.core.capture.CaptureDiagnostics> = diagnosticsState

    override fun syncCollectionEnabled(enabled: Boolean) {
        state.value = enabled
        diagnosticsState.value = diagnosticsState.value.copy(serviceRunning = enabled)
    }

    override fun refreshDiagnostics() = Unit
}
