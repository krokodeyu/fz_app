package com.example.frauddetector.core.capture

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.frauddetector.core.recording.DefaultEventRecordingPolicy
import com.example.frauddetector.core.recording.ObservableEventFilter
import com.example.frauddetector.data.store.CollectionSettingsStore
import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.usecase.ObserveCollectionSettingsUseCase
import com.example.frauddetector.domain.usecase.RecordBehaviorEventUseCase
import com.example.frauddetector.fixtures.CapturingBehaviorEventRepository
import com.example.frauddetector.fixtures.behaviorEvent
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventCaptureCoordinatorTest {

    @Test
    fun start_records_observable_event_from_real_source_flow() = runTest {
        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { File.createTempFile("capture", ".preferences_pb") }
        )
        val settingsStore = CollectionSettingsStore(dataStore)
        settingsStore.update { it.copy(collectionEnabled = true, recordingEnabled = true, observableOnly = true) }
        val repository = CapturingBehaviorEventRepository()
        val eventSource = TestEventSource()
        val coordinator = EventCaptureCoordinator(
            ObserveCollectionSettingsUseCase(settingsStore),
            eventSource,
            RecordBehaviorEventUseCase(repository, DefaultEventRecordingPolicy(ObservableEventFilter()))
        )

        coordinator.start()
        eventSource.emit(behaviorEvent(1_000L, "打开应用", app = "支付宝", appType = "金融类app"))
        advanceUntilIdle()
        coordinator.stop()

        assertEquals(1, repository.inserted.size)
        assertEquals("打开应用", repository.inserted.single().action)
    }

    private class TestEventSource : com.example.frauddetector.core.source.EventSource {
        private val flow = MutableSharedFlow<BehaviorEvent>()
        override val events: Flow<BehaviorEvent> = flow
        override fun start() = Unit
        override fun stop() = Unit
        suspend fun emit(event: BehaviorEvent) {
            flow.emit(event)
        }
    }
}
