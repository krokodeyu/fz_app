package com.example.frauddetector.core.detection

import com.example.frauddetector.core.recording.ObservableEventFilter
import com.example.frauddetector.core.schema.StandardBehaviorAction
import com.example.frauddetector.fixtures.behaviorEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObservableEventFilterTest {

    @Test
    fun observable_mapping_matches_canonical_action_catalog() {
        val filter = ObservableEventFilter()

        assertTrue(filter.isObservable(behaviorEvent(1L, StandardBehaviorAction.OPEN_APP.schemaAction)))
        assertTrue(filter.isObservable(behaviorEvent(1L, StandardBehaviorAction.CAMERA_ACTIVE.schemaAction)))
        assertFalse(filter.isObservable(behaviorEvent(1L, StandardBehaviorAction.TEXT_CHAT.schemaAction, observable = false)))
    }
}
