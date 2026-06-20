package com.tiswu.sagex.ui.home

import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tiswu.sagex.R
import com.tiswu.sagex.domain.TimeRange
import com.tiswu.sagex.service.AutostartHelper
import com.tiswu.sagex.ui.components.TimeRangeSelector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenConversations: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showClearDialog by remember { mutableStateOf(false) }
    var showAutostartGuidance by remember { mutableStateOf(false) }

    // Re-check notification access & battery exemption whenever the screen resumes.
    LifecycleResumeEffect(Unit) {
        viewModel.refreshStatus()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatusCard(state = state)

            Button(
                onClick = onOpenConversations,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("查看会话")
            }

            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("API 设置")
            }

            if (!state.accessGranted) {
                Button(
                    onClick = { context.startActivity(viewModel.settingsIntent()) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("开启通知访问")
                }
            }

            BackgroundCard(
                state = state,
                onToggleKeepAlive = viewModel::setKeepAlive,
                onRequestBattery = { context.startActivity(viewModel.batteryRequestIntent()) },
                onOpenAutostart = {
                    val launched = when (val result = viewModel.resolveAutostart()) {
                        is AutostartHelper.Result.Available ->
                            // resolveActivity() succeeding does not guarantee the activity is
                            // launchable (may be non-exported / permission-protected / removed).
                            runCatching { context.startActivity(result.intent) }.isSuccess
                        AutostartHelper.Result.None -> false
                    }
                    if (!launched) showAutostartGuidance = true
                },
            )

            OutlinedButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("删除消息")
            }
        }
    }

    if (showClearDialog) {
        var deleteRange by remember { mutableStateOf<TimeRange>(TimeRange.All) }
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("删除消息") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("选择时间范围,将永久删除该范围内所有会话的消息,无法恢复。")
                    TimeRangeSelector(selected = deleteRange, onSelected = { deleteRange = it })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteByRange(deleteRange)
                    showClearDialog = false
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            },
        )
    }

    if (showAutostartGuidance) {
        AlertDialog(
            onDismissRequest = { showAutostartGuidance = false },
            title = { Text("手动设置自启动") },
            text = {
                Text(
                    "未能直接打开本机的自启动设置。请到 系统设置 → 应用管理 → SageX," +
                        "开启「自启动 / 后台运行 / 允许后台活动」,并锁定后台以防被清理。",
                )
            },
            confirmButton = {
                TextButton(onClick = { showAutostartGuidance = false }) { Text("知道了") }
            },
        )
    }
}

@Composable
private fun StatusCard(state: HomeUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = if (state.accessGranted) "捕获中 · 已开启通知访问" else "未开启 · 需授予通知访问",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (state.accessGranted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
            Text(
                text = "已捕获消息:${state.totalMessages}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "最近捕获:" + formatLastCaptured(state.lastCapturedAt),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun BackgroundCard(
    state: HomeUiState,
    onToggleKeepAlive: (Boolean) -> Unit,
    onRequestBattery: () -> Unit,
    onOpenAutostart: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "后台保活",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "保持后台运行(常驻通知)",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Switch(
                    checked = state.keepAliveEnabled,
                    onCheckedChange = onToggleKeepAlive,
                )
            }

            Text(
                text = "电池优化白名单:" + if (state.batteryExempt) "已豁免" else "未豁免",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (!state.batteryExempt) {
                Button(onClick = onRequestBattery, modifier = Modifier.fillMaxWidth()) {
                    Text("加入电池白名单")
                }
            }

            OutlinedButton(onClick = onOpenAutostart, modifier = Modifier.fillMaxWidth()) {
                Text("设置自启动")
            }
        }
    }
}

private fun formatLastCaptured(timestamp: Long?): String {
    if (timestamp == null || timestamp <= 0L) return "暂无"
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()
}
