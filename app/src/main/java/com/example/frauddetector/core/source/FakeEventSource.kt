package com.example.frauddetector.core.source

import com.example.frauddetector.domain.model.BehaviorEvent
import javax.inject.Inject
import javax.inject.Singleton
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
        Sample("打开应用", "闲鱼", "电商类app", null, null),
        Sample("文本聊天", "闲鱼", "电商类app", null, null),
        Sample("打开应用", "支付宝", "金融类app", null, null),
        Sample("扫码", "支付宝", "金融类app", null, null),
        Sample("打开应用", "云闪付", "金融类app", null, null),
        Sample("购买商品", "云闪付", "金融类app", null, null),
        Sample("打开浏览器", "Chrome", "工具类app", "flash-sale.example", "电商网站"),
        Sample("切换前台应用", "相机", "工具类app", null, null)
    )
}
