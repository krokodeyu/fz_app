package com.example.frauddetector.presentation

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.frauddetector.core.aggregation.BehaviorWindowAggregator
import com.example.frauddetector.core.detection.DetectionResult
import com.example.frauddetector.core.source.EventSource
import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.repo.BehaviorEventRepository
import com.example.frauddetector.domain.usecase.AggregateWindowUseCase
import com.example.frauddetector.domain.usecase.ObserveRecentEventsUseCase
import com.example.frauddetector.domain.usecase.RunDetectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltViewModel
class MainViewModel @Inject constructor(
    private val observeRecentEventsUseCase: ObserveRecentEventsUseCase,
    private val aggregateWindowUseCase: AggregateWindowUseCase,
    private val runDetectionUseCase: RunDetectionUseCase,
    private val repository: BehaviorEventRepository,
    private val eventSource: EventSource,
    private val dataStore: DataStore<Preferences>,
    private val aggregator: BehaviorWindowAggregator
) : ViewModel() {

    private val enableCollectKey = booleanPreferencesKey("enable_collect")
    private val windowMillis = 5 * 60 * 1000L

    private val detectionState = MutableStateFlow(
        DetectionResult(
            stageAType = "other",
            stageAConfidence = 0.0,
            finalRisk = "NORMAL",
            reason = "等待事件输入中。"
        )
    )

    private val collectEnabledFlow = dataStore.data.map { it[enableCollectKey] ?: false }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val uiState: StateFlow<MainUiState> = combine(
        observeRecentEventsUseCase(limit = 30),
        collectEnabledFlow,
        detectionState
    ) { recentEvents, collectEnabled, detection ->
        val sequence = aggregateWindowUseCase(recentEvents, windowMillis)
        MainUiState(
            collectEnabled = collectEnabled,
            recentEvents = recentEvents,
            currentWindowText = aggregator.toText(sequence),
            detectionResult = detection
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            collectEnabledFlow.collect { enabled ->
                if (enabled) eventSource.start() else eventSource.stop()
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            eventSource.events.collect { event ->
                repository.insertEvent(event)
                // TODO: Add debounce/batch write strategy for better battery performance.
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            observeRecentEventsUseCase(limit = 30).collect { events ->
                val sequence = aggregateWindowUseCase(events, windowMillis)
                detectionState.value = runDetectionUseCase(sequence)
            }
        }
    }

    fun onCollectSwitchChanged(enabled: Boolean) {
        viewModelScope.launch {
            Timber.d("Collect switch changed: $enabled")
            dataStore.edit { it[enableCollectKey] = enabled }
        }
    }

    fun clearData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }
}

data class MainUiState(
    val collectEnabled: Boolean = false,
    val recentEvents: List<BehaviorEvent> = emptyList(),
    val currentWindowText: String = "",
    val detectionResult: DetectionResult = DetectionResult(
        stageAType = "other",
        stageAConfidence = 0.0,
        finalRisk = "NORMAL",
        reason = "等待事件输入中。"
    )
)
