package com.example.frauddetector.core.capture

import com.example.frauddetector.core.source.EventSource
import com.example.frauddetector.domain.model.CollectionSettings
import com.example.frauddetector.domain.usecase.ObserveCollectionSettingsUseCase
import com.example.frauddetector.domain.usecase.RecordBehaviorEventUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

@Singleton
class EventCaptureCoordinator @Inject constructor(
    observeCollectionSettingsUseCase: ObserveCollectionSettingsUseCase,
    private val eventSource: EventSource,
    private val recordBehaviorEventUseCase: RecordBehaviorEventUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val settingsState = MutableStateFlow(CollectionSettings())
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var settingsJob: Job? = null
    private var captureJob: Job? = null

    init {
        settingsJob = scope.launch {
            observeCollectionSettingsUseCase().collect { settingsState.value = it }
        }
    }

    fun start() {
        if (captureJob?.isActive == true) return
        eventSource.start()
        captureJob = scope.launch {
            _isRunning.value = true
            eventSource.events.collect { event ->
                val recorded = recordBehaviorEventUseCase(event, settingsState.value)
                if (!recorded) {
                    Timber.d("Event dropped by recording policy: %s", event)
                }
            }
        }
    }

    fun stop() {
        captureJob?.cancel()
        captureJob = null
        eventSource.stop()
        _isRunning.value = false
    }
}
