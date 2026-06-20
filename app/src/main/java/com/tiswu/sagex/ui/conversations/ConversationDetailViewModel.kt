package com.tiswu.sagex.ui.conversations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiswu.sagex.data.MessageRepository
import com.tiswu.sagex.domain.TimeRange
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

    companion object {
        const val ARG_CONVERSATION_KEY = "conversationKey"
    }
}
