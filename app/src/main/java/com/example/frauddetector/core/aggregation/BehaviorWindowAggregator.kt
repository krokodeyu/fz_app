package com.example.frauddetector.core.aggregation

import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.model.BehaviorSequence
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorWindowAggregator @Inject constructor() {

    fun aggregate(events: List<BehaviorEvent>, windowMillis: Long): BehaviorSequence {
        val now = System.currentTimeMillis()
        val start = now - windowMillis
        val inWindow = events.filter { it.timestamp in start..now }.sortedBy { it.timestamp }
        return BehaviorSequence(
            windowStart = start,
            windowEnd = now,
            events = inWindow
        )
    }

    fun toStruct(sequence: BehaviorSequence): List<Map<String, Any?>> {
        return sequence.events.map { event ->
            mapOf(
                "timestamp" to event.timestamp,
                "action" to event.action,
                "app" to event.app,
                "app_type" to event.appType,
                "website" to event.website,
                "website_type" to event.websiteType
            )
        }
    }

    fun toText(sequence: BehaviorSequence): String {
        if (sequence.events.isEmpty()) return ""
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sequence.events.joinToString(separator = "\n") { event ->
            val fields = buildList {
                add("动作=${event.action}")
                event.app?.let { add("APP=$it") }
                event.appType?.let { add("APP类型=$it") }
                event.website?.let { add("网站=$it") }
                event.websiteType?.let { add("网站类型=$it") }
            }
            "[${formatter.format(Date(event.timestamp))}] ${fields.joinToString(separator = " ")}"
        }
    }
}
