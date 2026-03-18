package com.example.frauddetector.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frauddetector.core.detection.DetectionResult
import com.example.frauddetector.core.recording.EventRecordingPolicy
import com.example.frauddetector.core.source.EventSource
import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.model.BehaviorTextProjection
import com.example.frauddetector.domain.model.CollectionSettings
import com.example.frauddetector.domain.usecase.AggregateWindowUseCase
import com.example.frauddetector.domain.usecase.BuildBehaviorTextUseCase
import com.example.frauddetector.domain.usecase.ObserveCollectionSettingsUseCase
import com.example.frauddetector.domain.usecase.ObserveRecentEventsUseCase
import com.example.frauddetector.domain.repo.BehaviorEventRepository
import com.example.frauddetector.domain.usecase.RecordBehaviorEventUseCase
import com.example.frauddetector.domain.usecase.RunDetectionUseCase
import com.example.frauddetector.domain.usecase.UpdateCollectionSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class MainViewModel @Inject constructor(
    private val observeRecentEventsUseCase: ObserveRecentEventsUseCase,
    private val observeCollectionSettingsUseCase: ObserveCollectionSettingsUseCase,
    private val updateCollectionSettingsUseCase: UpdateCollectionSettingsUseCase,
    private val aggregateWindowUseCase: AggregateWindowUseCase,
    private val buildBehaviorTextUseCase: BuildBehaviorTextUseCase,
    private val runDetectionUseCase: RunDetectionUseCase,
    private val recordBehaviorEventUseCase: RecordBehaviorEventUseCase,
    private val repository: BehaviorEventRepository,
    private val eventSource: EventSource,
    private val recordingPolicy: EventRecordingPolicy
) : ViewModel() {

    private val windowMillis = 5 * 60 * 1000L

    private val settingsFlow = observeCollectionSettingsUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, CollectionSettings())

    private val projectionState = MutableStateFlow(BehaviorTextProjection())

    private val detectionState = MutableStateFlow(defaultDetectionResult())

    val uiState: StateFlow<MainUiState> = combine(
        observeRecentEventsUseCase(limit = 30),
        settingsFlow,
        projectionState,
        detectionState
    ) { recentEvents, settings, projection, detection ->
        MainUiState(
            collectionEnabled = settings.collectionEnabled,
            recordingEnabled = settings.recordingEnabled,
            observableOnly = settings.observableOnly,
            textProjectionEnabled = settings.textProjectionEnabled,
            detectionEnabled = settings.detectionEnabled,
            recentEvents = recentEvents,
            projectedEvents = projection.projectedEvents,
            currentWindowText = projection.text,
            detectionResult = detection,
            recordingStatusText = buildRecordingStatusText(settings, projection)
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            settingsFlow.collect { settings ->
                if (settings.collectionEnabled) eventSource.start() else eventSource.stop()
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            eventSource.events.collect { event ->
                val recorded = recordBehaviorEventUseCase(event, settingsFlow.value)
                if (!recorded) {
                    Timber.d("Event dropped by recording policy: $event")
                }
                // TODO: Add debounce/batch write strategy for better battery performance.
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            combine(observeRecentEventsUseCase(limit = 30), settingsFlow) { events, settings ->
                events to settings
            }.collect { (events, settings) ->
                val sequence = aggregateWindowUseCase(events, windowMillis)
                val projection = buildBehaviorTextUseCase(sequence, settings)
                projectionState.value = projection

                val detectionEvents = projection.projectedEvents.filter {
                    recordingPolicy.shouldUseForDetection(it, settings)
                }
                val detectionSequence = sequence.copy(events = detectionEvents)
                detectionState.value = if (settings.detectionEnabled) {
                    runDetectionUseCase(detectionSequence)
                } else {
                    defaultDetectionResult(reason = "检测已关闭。")
                }
            }
        }
    }

    fun onCollectionSwitchChanged(enabled: Boolean) {
        viewModelScope.launch {
            Timber.d("Collection switch changed: $enabled")
            updateCollectionSettingsUseCase.updateCollectionEnabled(enabled)
        }
    }

    fun onRecordingSwitchChanged(enabled: Boolean) {
        viewModelScope.launch {
            Timber.d("Recording switch changed: $enabled")
            updateCollectionSettingsUseCase.updateRecordingEnabled(enabled)
        }
    }

    fun onObservableOnlySwitchChanged(enabled: Boolean) {
        viewModelScope.launch {
            Timber.d("Observable-only switch changed: $enabled")
            updateCollectionSettingsUseCase.updateObservableOnly(enabled)
        }
    }

    fun onTextProjectionSwitchChanged(enabled: Boolean) {
        viewModelScope.launch {
            Timber.d("Text projection switch changed: $enabled")
            updateCollectionSettingsUseCase.updateTextProjectionEnabled(enabled)
        }
    }

    fun onDetectionSwitchChanged(enabled: Boolean) {
        viewModelScope.launch {
            Timber.d("Detection switch changed: $enabled")
            updateCollectionSettingsUseCase.updateDetectionEnabled(enabled)
        }
    }

    fun clearData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }

    companion object {
        private fun defaultDetectionResult(reason: String = "等待事件输入中。") = DetectionResult(
            stageAType = "other",
            stageAConfidence = 0.0,
            finalRisk = "NORMAL",
            reason = reason
        )

        private fun buildRecordingStatusText(
            settings: CollectionSettings,
            projection: BehaviorTextProjection
        ): String {
            return buildString {
                append(if (settings.collectionEnabled) "采集中" else "采集关闭")
                append(" / ")
                append(if (settings.recordingEnabled) "记录开启" else "记录关闭")
                append(" / ")
                append(if (settings.observableOnly) "仅记录Observable" else "记录全部事件")
                append(" / 投影事件数=")
                append(projection.projectedEvents.size)
            }
        }
    }
}

data class MainUiState(
    val collectionEnabled: Boolean = false,
    val recordingEnabled: Boolean = true,
    val observableOnly: Boolean = true,
    val textProjectionEnabled: Boolean = true,
    val detectionEnabled: Boolean = true,
    val recentEvents: List<BehaviorEvent> = emptyList(),
    val projectedEvents: List<BehaviorEvent> = emptyList(),
    val currentWindowText: String = "",
    val detectionResult: DetectionResult = DetectionResult(
        stageAType = "other",
        stageAConfidence = 0.0,
        finalRisk = "NORMAL",
        reason = "等待事件输入中。"
    ),
    val recordingStatusText: String = "等待设置加载。"
)
