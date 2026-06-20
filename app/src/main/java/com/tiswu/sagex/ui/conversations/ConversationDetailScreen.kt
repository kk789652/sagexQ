package com.tiswu.sagex.ui.conversations

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tiswu.sagex.domain.TimeRange
import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationType
import com.tiswu.sagex.ui.components.Avatar
import com.tiswu.sagex.ui.components.TimeRangeSelector
import com.tiswu.sagex.ui.util.relativeTime
import com.tiswu.sagex.ui.util.shareText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    onBack: () -> Unit,
    onSummarize: () -> Unit,
    viewModel: ConversationDetailViewModel = hiltViewModel(),
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val title = messages.firstOrNull()?.conversationTitle ?: "会话"
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var messageToDelete by remember { mutableStateOf<CapturedMessage?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onSummarize) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "总结")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("导出/分享") },
                            onClick = {
                                showMenu = false
                                showExportDialog = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("删除本会话") },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                        )
                    }
                },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "暂无消息",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = messages,
                    key = { it.id },
                ) { message ->
                    MessageItem(
                        message = message,
                        showSender = message.conversationType == ConversationType.GROUP,
                        onLongClick = { messageToDelete = message },
                    )
                }
            }
        }
    }

    if (showExportDialog) {
        var exportRange by remember { mutableStateOf<TimeRange>(TimeRange.All) }
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("导出 / 分享") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("选择时间范围,将「$title」该范围内的聊天导出为文本,通过系统分享发给 ChatGPT 等 App。")
                    TimeRangeSelector(selected = exportRange, onSelected = { exportRange = it })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.export(exportRange) { text ->
                        if (text == null) {
                            Toast.makeText(context, "该时间范围内没有可导出的消息", Toast.LENGTH_SHORT).show()
                        } else {
                            shareText(context, text)
                        }
                    }
                    showExportDialog = false
                }) { Text("分享") }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("取消") }
            },
        )
    }

    if (showDeleteDialog) {
        var deleteRange by remember { mutableStateOf<TimeRange>(TimeRange.All) }
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除本会话消息") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("选择时间范围,将永久删除「$title」在该范围内的消息,无法恢复。")
                    TimeRangeSelector(selected = deleteRange, onSelected = { deleteRange = it })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteByRange(deleteRange)
                    showDeleteDialog = false
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("取消") }
            },
        )
    }

    messageToDelete?.let { message ->
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("删除消息") },
            text = { Text("将永久删除这条消息,无法恢复。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMessage(message.id)
                    messageToDelete = null
                }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageItem(
    message: CapturedMessage,
    showSender: Boolean,
    onLongClick: () -> Unit,
) {
    val avatarName = message.sender?.takeIf { it.isNotBlank() } ?: message.conversationTitle
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Avatar(name = avatarName)
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (showSender && !message.sender.isNullOrBlank()) {
                Text(
                    text = message.sender,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onLongClick,
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = message.body,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = relativeTime(message.postedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
