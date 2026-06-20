package com.tiswu.sagex.ui.home

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiswu.sagex.data.MessageRepository
import com.tiswu.sagex.notification.NotificationAccessHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val accessGranted: Boolean = false,
    val totalMessages: Int = 0,
    val lastCapturedAt: Long? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val accessHelper: NotificationAccessHelper,
) : ViewModel() {

    private val accessGranted = MutableStateFlow(accessHelper.isAccessGranted())

    val uiState: StateFlow<HomeUiState> = combine(
        accessGranted,
        repository.totalCount,
        repository.lastPostedAt,
    ) { granted, count, last ->
        HomeUiState(accessGranted = granted, totalMessages = count, lastCapturedAt = last)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(accessGranted = accessHelper.isAccessGranted()),
    )

    /** Re-check access; call when returning from the system settings screen. */
    fun refreshAccess() {
        accessGranted.value = accessHelper.isAccessGranted()
    }

    fun settingsIntent(): Intent = accessHelper.settingsIntent()

    fun clearAll() {
        viewModelScope.launch { repository.clearAll() }
    }
}
