package com.tiswu.sagex.ui.conversations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiswu.sagex.ai.TranscriptExporter
import com.tiswu.sagex.data.MessageRepository
import com.tiswu.sagex.data.prefs.ConversationReadStore
import com.tiswu.sagex.data.prefs.PromptSettings
import com.tiswu.sagex.domain.TimeRange
import com.tiswu.sagex.domain.filterByRange
import com.tiswu.sagex.domain.model.CapturedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationDetailViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val promptSettings: PromptSettings,
    private val readStore: ConversationReadStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val conversationKey: String =
        checkNotNull(savedStateHandle[ARG_CONVERSATION_KEY]) { "conversationKey is required" }

    val messages: StateFlow<List<CapturedMessage>> =
        repository.messages(conversationKey)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    /** Where to position the list on first display; [ReadPosition.Unknown] until loaded from disk. */
    private val _initialReadPosition = MutableStateFlow<ReadPosition>(ReadPosition.Unknown)
    val initialReadPosition: StateFlow<ReadPosition> = _initialReadPosition.asStateFlow()

    init {
        viewModelScope.launch {
            _initialReadPosition.value = ReadPosition.Loaded(readStore.lastReadId(conversationKey))
        }
    }

    /** Persists the message currently at the top of the viewport as the read position. */
    fun saveReadPosition(messageId: Long) {
        viewModelScope.launch { readStore.setLastReadId(conversationKey, messageId) }
    }

    fun deleteByRange(range: TimeRange) {
        viewModelScope.launch { repository.deleteByRange(conversationKey, range) }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch { repository.deleteMessage(id) }
    }

    /** Builds the export text for [range]; invokes [onReady] with null when the range is empty. */
    fun export(range: TimeRange, onReady: (ExportResult?) -> Unit) {
        viewModelScope.launch {
            val all = messages.value
            val inRange = filterByRange(all, range, System.currentTimeMillis())
            if (inRange.isEmpty()) {
                onReady(null)
                return@launch
            }
            val title = inRange.firstOrNull()?.conversationTitle ?: "会话"
            val sourcePackage = inRange.firstOrNull()?.sourcePackage.orEmpty()
            val instructionHeader = promptSettings.currentPrompt(sourcePackage)
                .takeIf { it.isNotBlank() }
                ?: PromptSettings.DEFAULT_PROMPT
            val included = minOf(inRange.size, TranscriptExporter.DEFAULT_MAX_MESSAGES)
            val text = TranscriptExporter.export(title, inRange, instructionHeader)
            onReady(ExportResult(text = text, total = inRange.size, included = included))
        }
    }

    companion object {
        const val ARG_CONVERSATION_KEY = "conversationKey"
    }
}

/** Result of building an export: the shareable [text] plus how many of [total] messages it covers. */
data class ExportResult(val text: String, val total: Int, val included: Int) {
    val truncated: Boolean get() = included < total
}

/** Initial scroll target for the conversation list. */
sealed interface ReadPosition {
    /** Persisted value not yet loaded — the UI should wait before deciding where to scroll. */
    data object Unknown : ReadPosition

    /** Loaded: [messageId] is the last read message, or null when there is no saved position. */
    data class Loaded(val messageId: Long?) : ReadPosition
}
