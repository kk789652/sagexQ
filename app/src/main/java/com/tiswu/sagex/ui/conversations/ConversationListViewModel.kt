package com.tiswu.sagex.ui.conversations

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiswu.sagex.data.MessageRepository
import com.tiswu.sagex.domain.appDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: MessageRepository,
) : ViewModel() {

    val sourcePackage: String =
        checkNotNull(savedStateHandle[ARG_SOURCE_PACKAGE]) { "Missing $ARG_SOURCE_PACKAGE" }

    val appName: String = appDisplayName(sourcePackage)

    val sections: StateFlow<List<ConversationSection>> =
        repository.conversations(sourcePackage)
            .map { ConversationGrouping.intoSections(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    companion object {
        const val ARG_SOURCE_PACKAGE = "sourcePackage"
    }
}
