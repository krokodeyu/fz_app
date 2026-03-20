package com.example.frauddetector.core.source

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.core.content.getSystemService
import com.example.frauddetector.core.collector.ForegroundAppCollector
import com.example.frauddetector.core.normalization.ObservableEventNormalizer
import com.example.frauddetector.domain.model.BehaviorEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

@Singleton
class UsageStatsEventSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val foregroundAppCollector: ForegroundAppCollector,
    private val normalizer: ObservableEventNormalizer
) : EventSource {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _events = MutableSharedFlow<BehaviorEvent>(extraBufferCapacity = 64)
    override val events: Flow<BehaviorEvent> = _events.asSharedFlow()

    private var collectionJob: Job? = null

    override fun start() {
        if (collectionJob?.isActive == true || !hasUsageStatsPermission(context)) return
        collectionJob = scope.launch {
            foregroundAppCollector.observe().collect { signal ->
                _events.emit(normalizer.normalize(signal))
            }
        }
    }

    override fun stop() {
        collectionJob?.cancel()
        collectionJob = null
    }

    companion object {
        fun hasUsageStatsPermission(context: Context): Boolean {
            val appOps = context.getSystemService<AppOpsManager>() ?: return false
            val mode = appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
            return mode == AppOpsManager.MODE_ALLOWED
        }

        fun usageAccessSettingsIntent(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }
}
