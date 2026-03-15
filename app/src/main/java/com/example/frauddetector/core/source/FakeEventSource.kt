package com.example.frauddetector.core.source

import com.example.frauddetector.domain.model.BehaviorEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeEventSource @Inject constructor() : EventSource {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _events = MutableSharedFlow<BehaviorEvent>(extraBufferCapacity = 64)
    override val events: Flow<BehaviorEvent> = _events.asSharedFlow()

    private var producerJob: Job? = null

    override fun start() {
        if (producerJob?.isActive == true) return
        producerJob = scope.launch {
            while (isActive) {
                _events.emit(generateEvent())
                delay(2_000)
            }
        }
    }

    override fun stop() {
        producerJob?.cancel()
        producerJob = null
    }

    private fun generateEvent(): BehaviorEvent {
        val template = samples.random()
        return BehaviorEvent(
            timestamp = System.currentTimeMillis(),
            action = template.action,
            app = template.app,
            appType = template.appType,
            website = template.website,
            websiteType = template.websiteType,
            source = "fake"
        )
    }

    private data class Sample(
        val action: String,
        val app: String?,
        val appType: String?,
        val website: String?,
        val websiteType: String?
    )

    private val samples = listOf(
        Sample("open_shop_offer", "ChatApp", "social", "flash-sale.example", "ecommerce"),
        Sample("click_task_reward", "MessageBox", "social", "reward-center.example", "task"),
        Sample("switch_foreground_app", "BankApp", "finance", null, null),
        Sample("open_browser", "Browser", "tools", "news.example", "news"),
        Sample("visit_adult_banner", "Browser", "tools", "adult-bait.example", "unknown")
    )
}
