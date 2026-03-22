package com.example.frauddetector.core.collector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.example.frauddetector.core.schema.ObservableSignal
import com.example.frauddetector.core.schema.StandardBehaviorAction
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class PackageChangeCollector @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun observe(): Flow<ObservableSignal> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val packageName = intent?.data?.schemeSpecificPart ?: return
                val timestamp = System.currentTimeMillis()
                val action = when (intent.action) {
                    Intent.ACTION_PACKAGE_ADDED -> {
                        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                            StandardBehaviorAction.UPDATE_APP
                        } else {
                            StandardBehaviorAction.INSTALL_APP
                        }
                    }
                    Intent.ACTION_PACKAGE_REMOVED -> {
                        if (intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) return
                        StandardBehaviorAction.UNINSTALL_APP
                    }
                    Intent.ACTION_PACKAGE_REPLACED -> StandardBehaviorAction.UPDATE_APP
                    else -> return
                }
                trySend(
                    ObservableSignal(
                        action = action,
                        timestamp = timestamp,
                        packageName = packageName,
                        source = "package_broadcast"
                    )
                )
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
