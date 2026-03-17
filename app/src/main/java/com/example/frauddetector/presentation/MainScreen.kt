package com.example.frauddetector.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.frauddetector.domain.model.BehaviorEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    state: MainUiState,
    onCollectChanged: (Boolean) -> Unit,
    onClearData: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "启用采集")
            Switch(checked = state.collectEnabled, onCheckedChange = onCollectChanged)
        }

        Button(onClick = onClearData) {
            Text("清空本地数据")
        }

        DetectionCard(state)

        Text(text = "最近事件", style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.recentEvents, key = { "${it.timestamp}-${it.action}" }) { event ->
                EventRow(event)
            }
        }
    }
}

@Composable
private fun DetectionCard(state: MainUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "当前窗口 text 输入", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (state.currentWindowText.isEmpty()) "暂无事件" else state.currentWindowText,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("StageA 类型: ${state.detectionResult.stageAType}")
            Text("StageA 置信度: ${"%.2f".format(state.detectionResult.stageAConfidence)}")
            Text("最终风险: ${state.detectionResult.finalRisk}")
            Text("解释: ${state.detectionResult.reason}")
        }
    }
}

@Composable
private fun EventRow(event: BehaviorEvent) {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = "[${formatter.format(Date(event.timestamp))}] ${event.action}")
            Text(text = "APP=${event.app ?: "-"} APP类型=${event.appType ?: "-"}")
            Text(text = "网站=${event.website ?: "-"} 网站类型=${event.websiteType ?: "-"}")
            Text(text = "source=${event.source}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
