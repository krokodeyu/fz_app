package com.example.frauddetector.core.normalization

import android.test.mock.MockContext
import com.example.frauddetector.core.schema.ObservableSignal
import com.example.frauddetector.core.schema.StandardBehaviorAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservableEventNormalizerTest {

    @Test
    fun normalize_maps_signal_to_behavior_event_schema() {
        val normalizer = ObservableEventNormalizer(
            object : AppMetadataResolver(MockContext()) {
                override fun resolveLabel(packageName: String?): String? = "支付宝"
                override fun resolveAppType(packageName: String?, label: String?): String = "金融类app"
                override fun isLikelyOnlineApp(packageName: String?): Boolean = true
            }
        )
        val signal = ObservableSignal(
            action = StandardBehaviorAction.OPEN_APP,
            timestamp = 1234L,
            packageName = "com.eg.android.AlipayGphone",
            information = mapOf("package_name" to "com.eg.android.AlipayGphone"),
            source = "usage_stats"
        )

        val event = normalizer.normalize(signal)

        assertEquals("打开应用", event.action)
        assertEquals("金融类app", event.appType)
        assertEquals("支付宝", event.app)
        assertTrue(event.observable)
        assertEquals("com.eg.android.AlipayGphone", event.packageName)
    }
}
