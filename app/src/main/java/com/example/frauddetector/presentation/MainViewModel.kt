package com.example.frauddetector.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frauddetector.core.capture.CollectionRuntimeController
import com.example.frauddetector.core.detection.DetectionResult
import com.example.frauddetector.core.export.BehaviorSeqJsonExporter
import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.model.BehaviorTextProjection
import com.example.frauddetector.domain.model.CollectionSettings
import com.example.frauddetector.domain.repo.BehaviorEventRepository
import com.example.frauddetector.domain.usecase.AggregateWindowUseCase
import com.example.frauddetector.domain.usecase.BuildBehaviorTextUseCase
import com.example.frauddetector.domain.usecase.ObserveCollectionSettingsUseCase
import com.example.frauddetector.domain.usecase.ObserveRecentEventsUseCase
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
    observeRecentEventsUseCase: ObserveRecentEventsUseCase,
    observeCollectionSettingsUseCase: ObserveCollectionSettingsUseCase,
    private val updateCollectionSettingsUseCase: UpdateCollectionSettingsUseCase,
    private val aggregateWindowUseCase: AggregateWindowUseCase,
    private val buildBehaviorTextUseCase: BuildBehaviorTextUseCase,
    private val runDetectionUseCase: RunDetectionUseCase,
    private val repository: BehaviorEventRepository,
    private val behaviorSeqJsonExporter: BehaviorSeqJsonExporter,
    private val collectionServiceController: CollectionRuntimeController
) : ViewModel() {

    private val windowMillis = 5 * 60 * 1000L

    private val settingsFlow = observeCollectionSettingsUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, CollectionSettings())

    private val recentEventsFlow = observeRecentEventsUseCase(limit = 30)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val serviceRunningFlow = collectionServiceController.serviceRunning
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val projectionState = MutableStateFlow(BehaviorTextProjection())
    private val detectionState = MutableStateFlow(defaultDetectionResult())
    private val exportJsonState = MutableStateFlow("")
    private val runtimeStatusFlow = combine(settingsFlow, serviceRunningFlow) { settings, serviceRunning ->
        settings to serviceRunning
    }

    val uiState: StateFlow<MainUiState> = combine(
        recentEventsFlow,
        projectionState,
        detectionState,
        exportJsonState,
        runtimeStatusFlow
    ) { recentEvents, projection, detection, exportJson, runtimeStatus ->
        val (settings, serviceRunning) = runtimeStatus
        MainUiState(
            collectionEnabled = settings.collectionEnabled,
            recordingEnabled = settings.recordingEnabled,
            observableOnly = settings.observableOnly,
            textProjectionEnabled = settings.textProjectionEnabled,
            detectionEnabled = settings.detectionEnabled,
            recentEvents = recentEvents,
            projectedEvents = projection.projectedEvents,
            currentWindowText = projection.text,
            exportJson = exportJson,
            detectionResult = detection,
            recordingStatusText = buildRecordingStatusText(settings, projection, serviceRunning),
            activeSourceLabel = if (serviceRunning) "REAL_DEVICE_FOREGROUND_SERVICE" else "REAL_DEVICE_IDLE"
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            settingsFlow.collect { settings ->
                collectionServiceController.syncCollectionEnabled(settings.collectionEnabled)
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            combine(recentEventsFlow, settingsFlow) { events, settings -> events to settings }
                .collect { (events, settings) ->
                    val sequence = aggregateWindowUseCase(events, windowMillis)
                    projectionState.value = buildBehaviorTextUseCase(sequence, settings)
                    exportJsonState.value = behaviorSeqJsonExporter.toJson(behaviorSeqJsonExporter.export(sequence))
                    detectionState.value = if (settings.detectionEnabled) {
                        runDetectionUseCase(sequence)
                    } else {
                        defaultDetectionResult(reason = "检测已关闭。")
                    }
                }
        }
    }

    fun onCollectionSwitchChanged(enabled: Boolean) {
        updateSetting("Collection", enabled) { updateCollectionSettingsUseCase.updateCollectionEnabled(enabled) }
    }

    fun onRecordingSwitchChanged(enabled: Boolean) {
        updateSetting("Recording", enabled) { updateCollectionSettingsUseCase.updateRecordingEnabled(enabled) }
    }

    fun onObservableOnlySwitchChanged(enabled: Boolean) {
        updateSetting("ObservableOnly", enabled) { updateCollectionSettingsUseCase.updateObservableOnly(enabled) }
    }

    fun onTextProjectionSwitchChanged(enabled: Boolean) {
        updateSetting("TextProjection", enabled) { updateCollectionSettingsUseCase.updateTextProjectionEnabled(enabled) }
    }

    fun onDetectionSwitchChanged(enabled: Boolean) {
        updateSetting("Detection", enabled) { updateCollectionSettingsUseCase.updateDetectionEnabled(enabled) }
    }

    fun clearData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }

    private fun updateSetting(label: String, enabled: Boolean, block: suspend () -> Unit) {
        viewModelScope.launch {
            Timber.d("%s switch changed: %s", label, enabled)
            block()
        }
    }

    companion object {
        private fun defaultDetectionResult(reason: String = "等待事件输入中。") = DetectionResult(
            riskLabel = "NORMAL",
            source = "RULE_BASED",
            reason = reason
        )

        private fun buildRecordingStatusText(
            settings: CollectionSettings,
            projection: BehaviorTextProjection,
            serviceRunning: Boolean
        ): String {
            return buildString {
                append(if (settings.collectionEnabled) "采集开关开启" else "采集开关关闭")
                append(" / ")
                append(if (serviceRunning) "后台监听已运行" else "后台监听未运行")
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
    val exportJson: String = "",
    val detectionResult: DetectionResult = DetectionResult(
        riskLabel = "NORMAL",
        source = "RULE_BASED",
        reason = "等待事件输入中。"
    ),
    val recordingStatusText: String = "等待设置加载。",
    val activeSourceLabel: String = "UNKNOWN"
)
