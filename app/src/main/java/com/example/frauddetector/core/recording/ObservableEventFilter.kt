package com.example.frauddetector.core.recording

import com.example.frauddetector.domain.model.BehaviorEvent
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservableEventFilter @Inject constructor() {

    fun filter(events: List<BehaviorEvent>, observableOnly: Boolean): List<BehaviorEvent> {
        return if (observableOnly) events.filter(::isObservable) else events
    }

    fun isObservable(event: BehaviorEvent): Boolean {
        val action = event.action.lowercase(Locale.ROOT)
        return observableActionKeywords.any(action::contains)
    }

    private val observableActionKeywords = listOf(
        "open",
        "switch",
        "install",
        "uninstall",
        "remove",
        "update",
        "visit",
        "scan",
        "camera",
        "foreground",
        "打开",
        "切换",
        "安装",
        "卸载",
        "更新",
        "扫码",
        "相机",
        "浏览器"
    )
}
