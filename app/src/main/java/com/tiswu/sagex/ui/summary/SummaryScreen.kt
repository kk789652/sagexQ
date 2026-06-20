package com.tiswu.sagex.ui.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tiswu.sagex.ui.components.TimeRangeSelector
import com.tiswu.sagex.ui.util.shareText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: SummaryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI 总结") },
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
            Text("时间范围", style = MaterialTheme.typography.titleSmall)
            TimeRangeSelector(
                selected = state.range,
                onSelected = viewModel::setRange,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = viewModel::generate,
                enabled = state.status != SummaryStatus.Loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("生成总结")
            }

            when (val status = state.status) {
                SummaryStatus.Idle -> Unit
                SummaryStatus.Loading -> Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                SummaryStatus.Empty -> StatusText("该时间范围内没有可总结的消息")

                SummaryStatus.NotConfigured -> Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatusText("尚未配置 API,请先在设置中填写 Base URL、API Key 和模型。")
                    OutlinedButton(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth()) {
                        Text("前往设置")
                    }
                }

                is SummaryStatus.Error -> StatusText(
                    text = "总结失败:${status.message}",
                    color = MaterialTheme.colorScheme.error,
                )

                is SummaryStatus.Success -> Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(status.text, style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(
                            onClick = { clipboard.setText(AnnotatedString(status.text)) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("复制")
                        }
                        OutlinedButton(
                            onClick = { shareText(context, status.text) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("分享")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusText(text: String, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(text = text, style = MaterialTheme.typography.bodyMedium, color = color)
}
