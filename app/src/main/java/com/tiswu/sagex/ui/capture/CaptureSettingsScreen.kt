package com.tiswu.sagex.ui.capture

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tiswu.sagex.service.AutostartHelper
import com.tiswu.sagex.ui.util.relativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureSettingsScreen(
    onBack: () -> Unit,
    viewModel: CaptureSettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAutostartGuidance by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.refreshStatus()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("捕获设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
            )
        },
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
                            runCatching { context.startActivity(result.intent) }.isSuccess
                        AutostartHelper.Result.None -> false
                    }
                    if (!launched) showAutostartGuidance = true
                },
            )
        }
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
private fun StatusCard(state: CaptureSettingsUiState) {
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
                text = "最近捕获:" + relativeTime(state.lastCapturedAt),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun BackgroundCard(
    state: CaptureSettingsUiState,
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
