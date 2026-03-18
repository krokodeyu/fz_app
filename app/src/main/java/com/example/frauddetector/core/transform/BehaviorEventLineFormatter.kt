package com.example.frauddetector.core.transform

import com.example.frauddetector.domain.model.BehaviorEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorEventLineFormatter @Inject constructor() {

    fun format(event: BehaviorEvent): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fields = buildList {
            add("动作=${event.action}")
            event.app?.takeIf { it.isNotBlank() }?.let { add("APP=$it") }
            event.appType?.takeIf { it.isNotBlank() }?.let { add("APP类型=$it") }
            event.website?.takeIf { it.isNotBlank() }?.let { add("网站=$it") }
            event.websiteType?.takeIf { it.isNotBlank() }?.let { add("网站类型=$it") }
        }
        return "[${formatter.format(Date(event.timestamp))}] ${fields.joinToString(separator = " ")}"
    }
}
