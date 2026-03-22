package com.example.frauddetector.presentation

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.frauddetector.core.export.BehaviorSeqJsonExporter
import com.example.frauddetector.core.recording.DefaultEventRecordingPolicy
import com.example.frauddetector.core.recording.ObservableEventFilter
import com.example.frauddetector.core.transform.BehaviorEventLineFormatter
import com.example.frauddetector.core.transform.BehaviorStructProjector
import com.example.frauddetector.core.transform.BehaviorTextProjector
import com.example.frauddetector.data.store.CollectionSettingsStore
import com.example.frauddetector.domain.usecase.AggregateWindowUseCase
import com.example.frauddetector.domain.usecase.BuildBehaviorTextUseCase
import com.example.frauddetector.domain.usecase.ObserveCollectionSettingsUseCase
import com.example.frauddetector.domain.usecase.ObserveRecentEventsUseCase
import com.example.frauddetector.domain.usecase.RunDetectionUseCase
import com.example.frauddetector.domain.usecase.UpdateCollectionSettingsUseCase
import com.example.frauddetector.fixtures.CountingBehaviorEventRepository
import com.example.frauddetector.fixtures.FakeCollectionRuntimeController
import com.example.frauddetector.fixtures.FakeFraudDetector
import com.example.frauddetector.fixtures.behaviorEvent
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @Test
    fun recentEventsFlow_is_shared_once_for_ui_and_detection() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val repository = CountingBehaviorEventRepository(
                events = listOf(behaviorEvent(1_000L, "打开应用", appType = "金融类app"))
            )
            val dataStore = PreferenceDataStoreFactory.create(
                produceFile = { File.createTempFile("settings", ".preferences_pb") }
            )
            val settingsStore = CollectionSettingsStore(dataStore)
            val observeSettings = ObserveCollectionSettingsUseCase(settingsStore)
            val updateSettings = UpdateCollectionSettingsUseCase(settingsStore)
            val policy = DefaultEventRecordingPolicy(ObservableEventFilter())
            val runtimeController = FakeCollectionRuntimeController()
            val viewModel = MainViewModel(
                observeRecentEventsUseCase = ObserveRecentEventsUseCase(repository),
                observeCollectionSettingsUseCase = observeSettings,
                updateCollectionSettingsUseCase = updateSettings,
                aggregateWindowUseCase = AggregateWindowUseCase(com.example.frauddetector.core.aggregation.BehaviorWindowAggregator()),
                buildBehaviorTextUseCase = BuildBehaviorTextUseCase(
                    com.example.frauddetector.core.export.BehaviorSeqAssembler(),
                    BehaviorTextProjector(BehaviorEventLineFormatter()),
                    BehaviorStructProjector(),
                    policy
                ),
                runDetectionUseCase = RunDetectionUseCase(FakeFraudDetector()),
                repository = repository,
                behaviorSeqJsonExporter = BehaviorSeqJsonExporter(com.example.frauddetector.core.export.BehaviorSeqAssembler(), BehaviorStructProjector()),
                collectionServiceController = runtimeController
            )

            viewModel.uiState.value
            testScheduler.advanceUntilIdle()

            assertEquals(1, repository.subscriptions.get())
            assertTrue(!viewModel.uiState.value.collectionEnabled)
        } finally {
            Dispatchers.resetMain()
        }
    }
}
