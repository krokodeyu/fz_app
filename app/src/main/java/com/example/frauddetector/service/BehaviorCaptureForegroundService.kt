package com.example.frauddetector.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.frauddetector.R
import com.example.frauddetector.core.capture.CollectionServiceController
import com.example.frauddetector.core.capture.EventCaptureCoordinator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BehaviorCaptureForegroundService : Service() {

    @Inject lateinit var coordinator: EventCaptureCoordinator
    @Inject lateinit var controller: CollectionServiceController

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action ?: ACTION_START) {
            ACTION_STOP -> {
                stopCapture()
                START_NOT_STICKY
            }
            else -> {
                startForeground(NOTIFICATION_ID, buildNotification())
                coordinator.start()
                controller.onServiceStateChanged(true)
                START_STICKY
            }
        }
    }

    override fun onDestroy() {
        controller.onServiceStateChanged(false)
        coordinator.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun stopCapture() {
        controller.onServiceStateChanged(false)
        coordinator.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle(getString(R.string.capture_notification_title))
            .setContentText(getString(R.string.capture_notification_text))
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.capture_notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.capture_notification_channel_description)
        }
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "fraud_capture"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_START = "com.example.frauddetector.action.START_CAPTURE"
        private const val ACTION_STOP = "com.example.frauddetector.action.STOP_CAPTURE"

        fun createStartIntent(context: Context): Intent {
            return Intent(context, BehaviorCaptureForegroundService::class.java).setAction(ACTION_START)
        }

        fun createStopIntent(context: Context): Intent {
            return Intent(context, BehaviorCaptureForegroundService::class.java).setAction(ACTION_STOP)
        }
    }
}
