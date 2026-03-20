package com.example.frauddetector.core.export

import com.example.frauddetector.domain.model.BehaviorSequence
import com.example.frauddetector.fixtures.behaviorEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BehaviorSeqAssemblerTest {

    @Test
    fun assemble_sorts_and_deduplicates_adjacent_events() {
        val assembler = BehaviorSeqAssembler()
        val sequence = BehaviorSequence(
            windowStart = 0L,
            windowEnd = 10_000L,
            events = listOf(
                behaviorEvent(3_000L, "打开应用", packageName = "pkg.a"),
                behaviorEvent(1_000L, "打开应用", packageName = "pkg.a"),
                behaviorEvent(2_000L, "打开应用", packageName = "pkg.a"),
                behaviorEvent(7_000L, "切换应用", packageName = "pkg.b")
            )
        )

        val assembled = assembler.assemble(sequence)

        assertEquals(2, assembled.size)
        assertTrue(assembled[0].timestamp < assembled[1].timestamp)
        assertEquals("切换应用", assembled[1].action)
    }

    @Test
    fun export_json_matches_training_friendly_schema_shape() {
        val exporter = BehaviorSeqJsonExporter(BehaviorSeqAssembler(), com.example.frauddetector.core.transform.BehaviorStructProjector())
        val sequence = BehaviorSequence(
            windowStart = 0L,
            windowEnd = 5_000L,
            events = listOf(behaviorEvent(1_000L, "打开应用", app = "支付宝", appType = "金融类app", packageName = "pkg"))
        )

        val export = exporter.export(sequence)
        val json = exporter.toJson(export)

        assertTrue(json.contains("\"behavior_seq\""))
        assertTrue(json.contains("\"score\":null"))
        assertTrue(json.contains("\"source\":\"device_capture\""))
        assertTrue(json.contains("\"observable\":true"))
    }
}
