package com.tiswu.sagex.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tiswu.sagex.R
import com.tiswu.sagex.domain.TimeRange
import com.tiswu.sagex.domain.model.AppSummary
import com.tiswu.sagex.ui.components.Avatar
import com.tiswu.sagex.ui.components.TimeRangeSelector
import com.tiswu.sagex.ui.util.relativeTime
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onOpenApp: (String) -> Unit = {},
    onOpenCaptureSettings: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenAbout: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showClearDialog by remember { mutableStateOf(false) }

    LifecycleResumeEffect(Unit) {
        viewModel.refreshStatus()
        onPauseOrDispose { }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawer(
                captureInactive = !state.accessGranted,
                onOpenCaptureSettings = {
                    scope.launch { drawerState.close() }
                    onOpenCaptureSettings()
                },
                onOpenSettings = {
                    scope.launch { drawerState.close() }
                    onOpenSettings()
                },
                onDeleteMessages = {
                    scope.launch { drawerState.close() }
                    showClearDialog = true
                },
                onOpenAbout = {
                    scope.launch { drawerState.close() }
                    onOpenAbout()
                },
            )
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "菜单")
                        }
                    },
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (!state.accessGranted) {
                    CaptureInactiveBanner()
                }

                if (state.apps.isEmpty()) {
                    EmptyApps(modifier = Modifier.weight(1f))
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.apps, key = { it.sourcePackage }) { app ->
                            AppRow(
                                app = app,
                                onClick = { onOpenApp(app.sourcePackage) },
                            )
                        }
                    }
                }
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
}

@Composable
private fun HomeDrawer(
    captureInactive: Boolean,
    onOpenCaptureSettings: () -> Unit,
    onOpenSettings: () -> Unit,
    onDeleteMessages: () -> Unit,
    onOpenAbout: () -> Unit,
) {
    ModalDrawerSheet {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(24.dp),
        )
        NavigationDrawerItem(
            label = { Text(if (captureInactive) "捕获设置 · 未开启" else "捕获设置") },
            selected = false,
            onClick = onOpenCaptureSettings,
            icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text("API 设置") },
            selected = false,
            onClick = onOpenSettings,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text("删除消息") },
            selected = false,
            onClick = onDeleteMessages,
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text("关于") },
            selected = false,
            onClick = onOpenAbout,
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

@Composable
private fun CaptureInactiveBanner() {
    Text(
        text = "通知访问未开启,当前不会捕获新消息",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onErrorContainer,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp, vertical = 10.dp),
    )
}

@Composable
private fun EmptyApps(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "还没有捕获到消息",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AppRow(app: AppSummary, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(name = app.displayName)
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = app.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${app.conversationCount} 个会话 · ${app.messageCount} 条消息",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = relativeTime(app.lastPostedAt),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 68.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    )
}
