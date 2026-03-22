package com.example.frauddetector.core.source.real

import com.example.frauddetector.core.collector.CameraUsageCollector
import com.example.frauddetector.core.collector.ForegroundAppCollector
import com.example.frauddetector.core.collector.PackageChangeCollector
import com.example.frauddetector.core.normalization.ObservableEventNormalizer
import com.example.frauddetector.core.source.EventSource
import com.example.frauddetector.domain.model.BehaviorEvent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import timber.log.Timber

@Singleton
class RealObservableEventSource @Inject constructor(
    private val foregroundAppCollector: ForegroundAppCollector,
    private val packageChangeCollector: PackageChangeCollector,
    private val cameraUsageCollector: CameraUsageCollector,
    private val normalizer: ObservableEventNormalizer
) : EventSource {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _events = MutableSharedFlow<BehaviorEvent>(extraBufferCapacity = 128)
    override val events: Flow<BehaviorEvent> = _events.asSharedFlow()

    private var collectionJob: Job? = null

    override fun start() {
        if (collectionJob?.isActive == true) return
        collectionJob = scope.launch {
            merge(
                foregroundAppCollector.observe(),
                packageChangeCollector.observe(),
                cameraUsageCollector.observe()
            ).collect { signal ->
                _events.emit(normalizer.normalize(signal))
            }
        }
    }

    override fun stop() {
        collectionJob?.cancel()
        collectionJob = null
    }

    fun hasForegroundPermission(): Boolean = foregroundAppCollector.hasPermission()

    init {
        if (!foregroundAppCollector.hasPermission()) {
            Timber.w("UsageStats permission not granted; foreground app collection will gracefully no-op until enabled.")
        }
    }
}
