package com.example.frauddetector

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.frauddetector.core.source.UsageStatsEventSource
import com.example.frauddetector.presentation.MainScreen
import com.example.frauddetector.presentation.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            Surface(color = MaterialTheme.colorScheme.background) {
                MainScreen(
                    state = state,
                    onCollectionChanged = viewModel::onCollectionSwitchChanged,
                    onRecordingChanged = viewModel::onRecordingSwitchChanged,
                    onObservableOnlyChanged = viewModel::onObservableOnlySwitchChanged,
                    onTextProjectionChanged = viewModel::onTextProjectionSwitchChanged,
                    onDetectionChanged = viewModel::onDetectionSwitchChanged,
                    onOpenUsageAccessSettings = ::openUsageAccessSettings,
                    onOpenNotificationSettings = ::openNotificationSettings,
                    onClearData = viewModel::clearData
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshRuntimeDiagnostics()
    }

    private fun openUsageAccessSettings() {
        startActivity(UsageStatsEventSource.usageAccessSettingsIntent())
    }

    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }
}
