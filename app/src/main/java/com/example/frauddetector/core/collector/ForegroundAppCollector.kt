package com.example.frauddetector.core.collector

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process
import androidx.core.content.getSystemService
import com.example.frauddetector.core.schema.ObservableSignal
import com.example.frauddetector.core.schema.StandardBehaviorAction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class ForegroundAppCollector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usageStatsManager: UsageStatsManager? = context.getSystemService()

    fun hasPermission(): Boolean {
        val appOps = context.getSystemService<AppOpsManager>() ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun observe(pollIntervalMillis: Long = 2_000L): Flow<ObservableSignal> = flow {
        if (!hasPermission()) return@flow
        var lastReadTimestamp = System.currentTimeMillis() - pollIntervalMillis
        var lastForegroundPackage: String? = null
        while (true) {
            val now = System.currentTimeMillis()
            val events = usageStatsManager?.queryEvents(lastReadTimestamp, now)
            if (events != null) {
                val event = UsageEvents.Event()
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        when (event.eventType) {
                            UsageEvents.Event.ACTIVITY_RESUMED -> {
                                val action = if (lastForegroundPackage == null || lastForegroundPackage == event.packageName) {
                                    StandardBehaviorAction.OPEN_APP
                                } else {
                                    StandardBehaviorAction.SWITCH_APP
                                }
                                emit(
                                    ObservableSignal(
                                        action = action,
                                        timestamp = event.timeStamp,
                                        packageName = event.packageName,
                                        previousPackageName = lastForegroundPackage,
                                        source = "usage_stats"
                                    )
                                )
                                lastForegroundPackage = event.packageName
                            }

                            UsageEvents.Event.ACTIVITY_PAUSED,
                            UsageEvents.Event.ACTIVITY_STOPPED -> {
                                emit(
                                    ObservableSignal(
                                        action = StandardBehaviorAction.CLOSE_APP,
                                        timestamp = event.timeStamp,
                                        packageName = event.packageName,
                                        source = "usage_stats"
                                    )
                                )
                            }
                        }
                    }
                }
            }
            lastReadTimestamp = now
            delay(pollIntervalMillis)
        }
    }
}
