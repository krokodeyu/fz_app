package com.example.frauddetector.core.collector

import android.app.AppOpsManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import com.example.frauddetector.core.schema.ObservableSignal
import com.example.frauddetector.core.schema.StandardBehaviorAction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import timber.log.Timber

@Singleton
class CameraUsageCollector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val appOpsManager: AppOpsManager? = context.getSystemService()

    fun observe(): Flow<ObservableSignal> = callbackFlow {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || appOpsManager == null) {
            close()
            return@callbackFlow
        }
        val listener = AppOpsManager.OnOpActiveChangedListener { op, uid, packageName, active ->
            if (op != AppOpsManager.OPSTR_CAMERA || !active) return@OnOpActiveChangedListener
            trySend(
                ObservableSignal(
                    action = StandardBehaviorAction.CAMERA_ACTIVE,
                    timestamp = System.currentTimeMillis(),
                    packageName = packageName,
                    information = mapOf("uid" to uid.toString()),
                    source = "app_ops_camera"
                )
            )
        }
        runCatching {
            appOpsManager.startWatchingActive(arrayOf(AppOpsManager.OPSTR_CAMERA), context.mainExecutor, listener)
        }.onFailure {
            Timber.w(it, "CameraUsageCollector unavailable on this device/ROM")
            close(it)
        }
        awaitClose {
            runCatching { appOpsManager.stopWatchingActive(listener) }
        }
    }
}
