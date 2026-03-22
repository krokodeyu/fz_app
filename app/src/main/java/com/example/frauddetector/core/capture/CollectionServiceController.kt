package com.example.frauddetector.core.capture

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
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
    }

    fun onServiceStateChanged(running: Boolean) {
        _serviceRunning.value = running
    }
}
