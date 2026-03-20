package com.example.frauddetector.core.source

import com.example.frauddetector.core.schema.StandardBehaviorAction
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

/**
 * Demo-only event source for debug builds.
 * Do not use this source as the default release path.
 */
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
            action = template.action.schemaAction,
            app = template.app,
            appType = template.appType,
            website = template.website,
            websiteType = template.websiteType,
            information = template.information,
            online = template.online,
            observable = template.action.observable,
            source = "demo_fake_source",
            packageName = template.packageName
        )
    }

    private data class Sample(
        val action: StandardBehaviorAction,
        val app: String?,
        val appType: String?,
        val website: String?,
        val websiteType: String?,
        val information: Map<String, String> = emptyMap(),
        val online: Boolean = true,
        val packageName: String? = null
    )

    private val samples = listOf(
        Sample(StandardBehaviorAction.OPEN_APP, "闲鱼", "电商类app", null, null, packageName = "com.taobao.idlefish"),
        Sample(StandardBehaviorAction.TEXT_CHAT, "闲鱼", "电商类app", null, null, information = mapOf("summary" to "购物事宜"), packageName = "com.taobao.idlefish"),
        Sample(StandardBehaviorAction.OPEN_APP, "支付宝", "金融类app", null, null, packageName = "com.eg.android.AlipayGphone"),
        Sample(StandardBehaviorAction.OPEN_CAMERA, "相机", "工具类app", null, null, packageName = "com.android.camera"),
        Sample(StandardBehaviorAction.CAMERA_ACTIVE, "相机", "工具类app", null, null, packageName = "com.android.camera"),
        Sample(StandardBehaviorAction.OPEN_BROWSER, "Chrome", "浏览器类app", "flash-sale.example", "电商网站", packageName = "com.android.chrome"),
        Sample(StandardBehaviorAction.SWITCH_APP, "云闪付", "金融类app", null, null, packageName = "com.unionpay")
    )
}
