package com.example.frauddetector.core.transform

import com.example.frauddetector.domain.model.BehaviorEvent
import com.example.frauddetector.domain.model.BehaviorSequence
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorStructProjector @Inject constructor() {

    fun project(sequence: BehaviorSequence): List<Map<String, Any?>> = project(sequence.events)

    fun project(events: List<BehaviorEvent>): List<Map<String, Any?>> {
        return events.map { event ->
            mapOf(
                "timestamp" to event.timestamp,
                "action" to event.action,
                "app" to event.app,
                "app_type" to event.appType,
                "website" to event.website,
                "website_type" to event.websiteType,
                "information" to event.information,
                "online" to event.online,
                "observable" to event.observable
            )
        }
    }
}
