package com.tiswu.sagex.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiswu.sagex.data.MessageRepository
import com.tiswu.sagex.domain.TimeRange
import com.tiswu.sagex.domain.model.AppSummary
import com.tiswu.sagex.notification.NotificationAccessHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    val feishuSyncStatus: FeishuSyncStatus = FeishuSyncStatus.Idle,
)

sealed interface FeishuSyncStatus {
    object Idle : FeishuSyncStatus
    object Syncing : FeishuSyncStatus
    data class Done(val count: Int) : FeishuSyncStatus
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val accessHelper: NotificationAccessHelper,
) : ViewModel() {

    private val accessGranted = MutableStateFlow(accessHelper.isAccessGranted())
    private val _feishuSyncStatus = MutableStateFlow<FeishuSyncStatus>(FeishuSyncStatus.Idle)

    val uiState: StateFlow<HomeUiState> = combine(
        accessGranted,
        repository.apps,
        _feishuSyncStatus,
    ) { granted, apps, syncStatus ->
        HomeUiState(
            accessGranted = granted,
            apps = apps,
            feishuSyncStatus = syncStatus,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(
            accessGranted = accessHelper.isAccessGranted(),
            feishuSyncStatus = _feishuSyncStatus.value,
        ),
    )

    /** Re-check OS-level state; call when returning from a system settings screen. */
    fun refreshStatus() {
        accessGranted.value = accessHelper.isAccessGranted()
    }

    fun deleteByRange(range: TimeRange) {
        viewModelScope.launch { repository.deleteByRange(range) }
    }

    fun syncToFeishu() {
        if (_feishuSyncStatus.value is FeishuSyncStatus.Syncing) return

        viewModelScope.launch {
            _feishuSyncStatus.value = FeishuSyncStatus.Syncing
            val count = repository.syncUnsynced()
            _feishuSyncStatus.value = FeishuSyncStatus.Done(count)
            delay(3_000)
            _feishuSyncStatus.value = FeishuSyncStatus.Idle
        }
    }
}
