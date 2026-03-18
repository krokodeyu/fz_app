package com.example.frauddetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.frauddetector.presentation.MainScreen
import com.example.frauddetector.presentation.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            Surface(color = MaterialTheme.colorScheme.background) {
                MainScreen(
                    state = state,
                    onCollectionChanged = viewModel::onCollectionSwitchChanged,
                    onRecordingChanged = viewModel::onRecordingSwitchChanged,
                    onObservableOnlyChanged = viewModel::onObservableOnlySwitchChanged,
                    onTextProjectionChanged = viewModel::onTextProjectionSwitchChanged,
                    onDetectionChanged = viewModel::onDetectionSwitchChanged,
                    onClearData = viewModel::clearData
                )
            }
        }
    }
}
