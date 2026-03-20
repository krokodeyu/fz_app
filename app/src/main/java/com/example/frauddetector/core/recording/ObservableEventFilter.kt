package com.example.frauddetector.core.recording

import com.example.frauddetector.core.schema.StandardBehaviorAction
import com.example.frauddetector.domain.model.BehaviorEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservableEventFilter @Inject constructor() {

    fun filter(events: List<BehaviorEvent>, observableOnly: Boolean): List<BehaviorEvent> {
        return if (observableOnly) events.filter(::isObservable) else events
    }

    fun isObservable(event: BehaviorEvent): Boolean {
        val mapped = StandardBehaviorAction.fromActionText(event.action)
        return mapped?.observable ?: event.observable
    }
}
