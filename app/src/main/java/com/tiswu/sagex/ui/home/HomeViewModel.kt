package com.tiswu.sagex.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiswu.sagex.data.MessageRepository
import com.tiswu.sagex.domain.TimeRange
import com.tiswu.sagex.domain.model.AppSummary
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
    val apps: List<AppSummary> = emptyList(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val accessHelper: NotificationAccessHelper,
) : ViewModel() {

    private val accessGranted = MutableStateFlow(accessHelper.isAccessGranted())

    val uiState: StateFlow<HomeUiState> = combine(
        accessGranted,
        repository.apps,
    ) { granted, apps ->
        HomeUiState(
            accessGranted = granted,
            apps = apps,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(
            accessGranted = accessHelper.isAccessGranted(),
        ),
    )

    /** Re-check OS-level state; call when returning from a system settings screen. */
    fun refreshStatus() {
        accessGranted.value = accessHelper.isAccessGranted()
    }

    fun deleteByRange(range: TimeRange) {
        viewModelScope.launch { repository.deleteByRange(range) }
    }
}
