package com.example.frauddetector.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
    onCollectionChanged: (Boolean) -> Unit,
    onRecordingChanged: (Boolean) -> Unit,
    onObservableOnlyChanged: (Boolean) -> Unit,
    onTextProjectionChanged: (Boolean) -> Unit,
    onDetectionChanged: (Boolean) -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onClearData: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SettingsCard(
                state = state,
                onCollectionChanged = onCollectionChanged,
                onRecordingChanged = onRecordingChanged,
                onObservableOnlyChanged = onObservableOnlyChanged,
                onTextProjectionChanged = onTextProjectionChanged,
                onDetectionChanged = onDetectionChanged,
                onOpenUsageAccessSettings = onOpenUsageAccessSettings,
                onOpenNotificationSettings = onOpenNotificationSettings,
                onClearData = onClearData
            )
        }
        item { DetectionCard(state) }
        item { ExportCard(state.exportJson) }
        item { EventSection(title = "参与文本生成的事件", events = state.projectedEvents) }
        item { EventSection(title = "最近原始事件", events = state.recentEvents) }
    }
}

@Composable
private fun SettingsCard(
    state: MainUiState,
    onCollectionChanged: (Boolean) -> Unit,
    onRecordingChanged: (Boolean) -> Unit,
    onObservableOnlyChanged: (Boolean) -> Unit,
    onTextProjectionChanged: (Boolean) -> Unit,
    onDetectionChanged: (Boolean) -> Unit,
    onOpenUsageAccessSettings: () -> Unit,
    onOpenNotificationSettings: () -> Unit,
    onClearData: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "事件记录设置", style = MaterialTheme.typography.titleMedium)
            Text(text = "当前数据源: ${state.activeSourceLabel}", style = MaterialTheme.typography.bodySmall)
            Text(
                text = when {
                    !state.usageAccessGranted -> "当前无法采集前台 app 打开/关闭/切换：请先授予 Usage Access 权限。"
                    !state.notificationPermissionGranted -> "前台服务通知权限未开启，后台常驻采集可能被系统限制。"
                    else -> "前台 app / 安装卸载 / 相机活跃采集链路已就绪。"
                },
                style = MaterialTheme.typography.bodySmall
            )
            SettingRow(label = "启用采集（启动/停止后台监听）", checked = state.collectionEnabled, onCheckedChange = onCollectionChanged)
            SettingRow(label = "启用记录（写入本地 Room）", checked = state.recordingEnabled, onCheckedChange = onRecordingChanged)
            SettingRow(label = "仅记录 Observable", checked = state.observableOnly, onCheckedChange = onObservableOnlyChanged)
            SettingRow(label = "启用文本生成（仅影响 text 投影）", checked = state.textProjectionEnabled, onCheckedChange = onTextProjectionChanged)
            SettingRow(label = "启用检测", checked = state.detectionEnabled, onCheckedChange = onDetectionChanged)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onOpenUsageAccessSettings) { Text("打开 Usage Access") }
                Button(onClick = onOpenNotificationSettings) { Text("打开通知设置") }
            }
            Text(text = state.recordingStatusText, style = MaterialTheme.typography.bodySmall)
            Button(onClick = onClearData) { Text("清空本地数据") }
        }
    }
}

@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DetectionCard(state: MainUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "当前窗口 text 输入", style = MaterialTheme.typography.titleSmall)
            Text(
                text = if (state.currentWindowText.isEmpty()) "暂无可投影事件" else state.currentWindowText,
                style = MaterialTheme.typography.bodySmall
            )
            Text("风险标签: ${state.detectionResult.riskLabel}")
            Text("诈骗子类型: ${state.detectionResult.fraudSubtype ?: "-"}")
            Text("检测来源: ${state.detectionResult.source}")
            Text("解释: ${state.detectionResult.reason}")
            if (state.detectionResult.evidence.isNotEmpty()) {
                Text("证据: ${state.detectionResult.evidence.joinToString()}")
            }
        }
    }
}

@Composable
private fun ExportCard(exportJson: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "behavior_seq JSON 导出", style = MaterialTheme.typography.titleSmall)
            Text(
                text = if (exportJson.isBlank()) "暂无导出数据" else exportJson,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun EventSection(
    title: String,
    events: List<BehaviorEvent>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            if (events.isEmpty()) {
                Text(text = "暂无事件", style = MaterialTheme.typography.bodySmall)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    events.forEach { event -> EventRow(event) }
                }
            }
        }
    }
}

@Composable
private fun EventRow(event: BehaviorEvent) {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "[${formatter.format(Date(event.timestamp))}] ${event.action}")
            Text(text = "APP=${event.app ?: "-"} APP类型=${event.appType ?: "-"}")
            Text(text = "网站=${event.website ?: "-"} 网站类型=${event.websiteType ?: "-"}")
            Text(text = "observable=${event.observable} online=${event.online}")
            if (event.information.isNotEmpty()) {
                Text(text = "information=${event.information}")
            }
            Text(text = "source=${event.source}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
