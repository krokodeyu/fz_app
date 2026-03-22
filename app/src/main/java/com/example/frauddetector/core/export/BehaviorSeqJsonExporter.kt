package com.example.frauddetector.core.export

import com.example.frauddetector.core.transform.BehaviorStructProjector
import com.example.frauddetector.domain.model.BehaviorSequence
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorSeqJsonExporter @Inject constructor(
    private val behaviorSeqAssembler: BehaviorSeqAssembler,
    private val structProjector: BehaviorStructProjector
) {

    fun export(sequence: BehaviorSequence): BehaviorSeqExport {
        val assembledEvents = behaviorSeqAssembler.assemble(sequence)
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val behaviorSeq = structProjector.project(assembledEvents).mapIndexed { index, event ->
            event.toMutableMap().apply {
                this["timestamp"] = formatter.format(Date(assembledEvents[index].timestamp))
                this["information"] = assembledEvents[index].information
                this["online"] = assembledEvents[index].online
                this["observable"] = assembledEvents[index].observable
            }
        }
        return BehaviorSeqExport(
            behaviorSeq = behaviorSeq,
            score = null,
            source = "device_capture",
            caseType = null,
            meta = mapOf(
                "window_start" to formatter.format(Date(sequence.windowStart)),
                "window_end" to formatter.format(Date(sequence.windowEnd)),
                "event_count" to assembledEvents.size,
                "window_millis" to (sequence.windowEnd - sequence.windowStart)
            )
        )
    }

    fun toJson(export: BehaviorSeqExport): String {
        val behaviorSeqJson = export.behaviorSeq.joinToString(prefix = "[", postfix = "]") { event ->
            event.entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
                "\"${escape(key)}\":${toJsonValue(value)}"
            }
        }
        val metaJson = export.meta.entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
            "\"${escape(key)}\":${toJsonValue(value)}"
        }
        return buildString {
            append("{")
            append("\"behavior_seq\":")
            append(behaviorSeqJson)
            append(",\"score\":null")
            append(",\"source\":\"${escape(export.source)}\"")
            append(",\"case_type\":null")
            append(",\"meta\":")
            append(metaJson)
            append("}")
        }
    }

    private fun toJsonValue(value: Any?): String = when (value) {
        null -> "null"
        is Number, is Boolean -> value.toString()
        is Map<*, *> -> value.entries.joinToString(prefix = "{", postfix = "}") { (key, entryValue) ->
            "\"${escape(key.toString())}\":${toJsonValue(entryValue)}"
        }
        is Iterable<*> -> value.joinToString(prefix = "[", postfix = "]") { toJsonValue(it) }
        else -> "\"${escape(value.toString())}\""
    }

    private fun escape(value: String): String = value.replace("\\", "\\\\").replace("\"", "\\\"")
}
