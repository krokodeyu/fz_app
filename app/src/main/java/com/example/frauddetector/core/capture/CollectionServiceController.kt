package com.example.frauddetector.core.capture

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.frauddetector.core.source.UsageStatsEventSource
import com.example.frauddetector.service.BehaviorCaptureForegroundService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class CollectionServiceController @Inject constructor(
    @ApplicationContext private val context: Context
) : CollectionRuntimeController {
    private val _serviceRunning = MutableStateFlow(false)
    override val serviceRunning: StateFlow<Boolean> = _serviceRunning.asStateFlow()

    private val _diagnostics = MutableStateFlow(buildDiagnostics())
    override val diagnostics: StateFlow<CaptureDiagnostics> = _diagnostics.asStateFlow()

    override fun syncCollectionEnabled(enabled: Boolean) {
        if (enabled) {
            ContextCompat.startForegroundService(
                context,
                BehaviorCaptureForegroundService.createStartIntent(context)
            )
        } else {
            context.startService(BehaviorCaptureForegroundService.createStopIntent(context))
            context.stopService(Intent(context, BehaviorCaptureForegroundService::class.java))
            _serviceRunning.value = false
        }
        refreshDiagnostics()
    }

    override fun refreshDiagnostics() {
        _diagnostics.value = buildDiagnostics()
    }

    fun onServiceStateChanged(running: Boolean) {
        _serviceRunning.value = running
        refreshDiagnostics()
    }

    private fun buildDiagnostics(): CaptureDiagnostics {
        return CaptureDiagnostics(
            serviceRunning = _serviceRunning.value,
            usageAccessGranted = UsageStatsEventSource.hasUsageStatsPermission(context),
            notificationPermissionGranted = hasNotificationPermission(),
            cameraMonitoringSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        )
    }

    private fun hasNotificationPermission(): Boolean {
        val runtimeGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return runtimeGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}
