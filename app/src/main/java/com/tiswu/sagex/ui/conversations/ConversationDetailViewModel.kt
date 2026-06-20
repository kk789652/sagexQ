package com.tiswu.sagex.ui.conversations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiswu.sagex.ai.TranscriptExporter
import com.tiswu.sagex.data.MessageRepository
import com.tiswu.sagex.data.prefs.PromptSettings
import com.tiswu.sagex.domain.TimeRange
import com.tiswu.sagex.domain.filterByRange
import com.tiswu.sagex.domain.model.CapturedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationDetailViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val promptSettings: PromptSettings,
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

    fun deleteByRange(range: TimeRange) {
        viewModelScope.launch { repository.deleteByRange(conversationKey, range) }
    }

    fun deleteMessage(id: Long) {
        viewModelScope.launch { repository.deleteMessage(id) }
    }

    /** Builds the export text for [range]; invokes [onReady] with null when the range is empty. */
    fun export(range: TimeRange, onReady: (String?) -> Unit) {
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
            onReady(TranscriptExporter.export(title, inRange, instructionHeader))
        }
    }

    companion object {
        const val ARG_CONVERSATION_KEY = "conversationKey"
    }
}
