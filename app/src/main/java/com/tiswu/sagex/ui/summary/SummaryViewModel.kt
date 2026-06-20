package com.tiswu.sagex.ui.summary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiswu.sagex.ai.OpenAiClient
import com.tiswu.sagex.ai.SummaryPromptBuilder
import com.tiswu.sagex.data.MessageRepository
import com.tiswu.sagex.data.prefs.ApiSettings
import com.tiswu.sagex.data.prefs.PromptSettings
import com.tiswu.sagex.domain.TimeRange
import com.tiswu.sagex.domain.filterByRange
import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.ui.conversations.ConversationDetailViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
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
    val range: TimeRange = TimeRange.All,
    val candidates: List<CapturedMessage> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val status: SummaryStatus = SummaryStatus.Idle,
)

internal fun selectMessages(
    candidates: List<CapturedMessage>,
    selectedIds: Set<Long>,
): List<CapturedMessage> = candidates.filter { it.id in selectedIds }

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val apiSettings: ApiSettings,
    private val promptSettings: PromptSettings,
    private val client: OpenAiClient,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val conversationKey: String =
        checkNotNull(savedStateHandle[ConversationDetailViewModel.ARG_CONVERSATION_KEY])

    private val messages: StateFlow<List<CapturedMessage>> =
        repository.messages(conversationKey)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val range = MutableStateFlow<TimeRange>(TimeRange.All)

    val candidates: StateFlow<List<CapturedMessage>> =
        combine(messages, range) { messages, range ->
            filterByRange(messages, range, System.currentTimeMillis())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val selectedIds = MutableStateFlow<Set<Long>>(emptySet())
    private val status = MutableStateFlow<SummaryStatus>(SummaryStatus.Idle)

    val uiState: StateFlow<SummaryUiState> =
        combine(range, candidates, selectedIds, status) { range, candidates, selectedIds, status ->
            SummaryUiState(
                range = range,
                candidates = candidates,
                selectedIds = selectedIds,
                status = status,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SummaryUiState())

    init {
        viewModelScope.launch {
            candidates.collect { messages ->
                selectedIds.value = messages.map { it.id }.toSet()
            }
        }
    }

    fun setRange(range: TimeRange) {
        this.range.value = range
    }

    fun toggle(id: Long) {
        selectedIds.update { ids ->
            if (id in ids) ids - id else ids + id
        }
    }

    fun selectAll() {
        selectedIds.value = candidates.value.map { it.id }.toSet()
    }

    fun clearAll() {
        selectedIds.value = emptySet()
    }

    fun generate() {
        viewModelScope.launch {
            val config = apiSettings.current()
            if (!config.isConfigured) {
                status.value = SummaryStatus.NotConfigured
                return@launch
            }
            val selected = selectMessages(candidates.value, selectedIds.value)
            if (selected.isEmpty()) {
                status.value = SummaryStatus.Empty
                return@launch
            }
            val title = selected.first().conversationTitle
            val sourcePackage = selected.first().sourcePackage
            val systemPrompt = promptSettings.currentPrompt(sourcePackage)
                .ifBlank { PromptSettings.DEFAULT_PROMPT }
            val prompt = SummaryPromptBuilder.build(
                conversationTitle = title,
                messages = selected,
                systemPrompt = systemPrompt,
            )

            status.value = SummaryStatus.Loading
            val result = client.summarize(config, prompt)
            status.value = result.fold(
                onSuccess = { text -> SummaryStatus.Success(text) },
                onFailure = { e -> SummaryStatus.Error(e.message ?: "未知错误") },
            )
        }
    }
}
