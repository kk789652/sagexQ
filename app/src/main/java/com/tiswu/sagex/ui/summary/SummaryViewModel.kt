package com.tiswu.sagex.ui.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiswu.sagex.ai.MessageRange
import com.tiswu.sagex.ai.OpenAiClient
import com.tiswu.sagex.ai.SummaryPromptBuilder
import com.tiswu.sagex.ai.filterByRange
import com.tiswu.sagex.data.MessageRepository
import com.tiswu.sagex.data.prefs.ApiSettings
import com.tiswu.sagex.ui.conversations.ConversationDetailViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SummaryStatus {
    data object Idle : SummaryStatus
    data object Loading : SummaryStatus
    data object NotConfigured : SummaryStatus
    data object Empty : SummaryStatus
    data class Success(val text: String) : SummaryStatus
    data class Error(val message: String) : SummaryStatus
}

data class SummaryUiState(
    val range: MessageRange = MessageRange.ALL,
    val status: SummaryStatus = SummaryStatus.Idle,
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val apiSettings: ApiSettings,
    private val client: OpenAiClient,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val conversationKey: String =
        checkNotNull(savedStateHandle[ConversationDetailViewModel.ARG_CONVERSATION_KEY])

    private val _state = MutableStateFlow(SummaryUiState())
    val state: StateFlow<SummaryUiState> = _state.asStateFlow()

    fun setRange(range: MessageRange) {
        _state.update { it.copy(range = range) }
    }

    fun generate() {
        viewModelScope.launch {
            val config = apiSettings.current()
            if (!config.isConfigured) {
                _state.update { it.copy(status = SummaryStatus.NotConfigured) }
                return@launch
            }
            val messages = repository.messages(conversationKey).first()
            val inRange = filterByRange(messages, _state.value.range, System.currentTimeMillis())
            if (inRange.isEmpty()) {
                _state.update { it.copy(status = SummaryStatus.Empty) }
                return@launch
            }
            val title = messages.firstOrNull()?.conversationTitle ?: "会话"
            val prompt = SummaryPromptBuilder.build(title, inRange)

            _state.update { it.copy(status = SummaryStatus.Loading) }
            val result = client.summarize(config, prompt)
            _state.update {
                it.copy(
                    status = result.fold(
                        onSuccess = { text -> SummaryStatus.Success(text) },
                        onFailure = { e -> SummaryStatus.Error(e.message ?: "未知错误") },
                    ),
                )
            }
        }
    }
}
