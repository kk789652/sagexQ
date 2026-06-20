package com.tiswu.sagex.ui.capture

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiswu.sagex.data.MessageRepository
import com.tiswu.sagex.data.prefs.KeepAlivePreferences
import com.tiswu.sagex.notification.NotificationAccessHelper
import com.tiswu.sagex.service.AutostartHelper
import com.tiswu.sagex.service.BatteryOptimizationHelper
import com.tiswu.sagex.service.ServiceController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CaptureSettingsUiState(
    val accessGranted: Boolean = false,
    val totalMessages: Int = 0,
    val lastCapturedAt: Long? = null,
    val keepAliveEnabled: Boolean = false,
    val batteryExempt: Boolean = false,
)

@HiltViewModel
class CaptureSettingsViewModel @Inject constructor(
    private val repository: MessageRepository,
    private val accessHelper: NotificationAccessHelper,
    private val keepAlivePreferences: KeepAlivePreferences,
    private val serviceController: ServiceController,
    private val batteryHelper: BatteryOptimizationHelper,
    private val autostartHelper: AutostartHelper,
) : ViewModel() {

    private val accessGranted = MutableStateFlow(accessHelper.isAccessGranted())
    private val batteryExempt = MutableStateFlow(batteryHelper.isIgnoringBatteryOptimizations())

    val uiState: StateFlow<CaptureSettingsUiState> = combine(
        accessGranted,
        repository.totalCount,
        repository.lastPostedAt,
        keepAlivePreferences.enabled,
        batteryExempt,
    ) { granted, count, last, keepAlive, exempt ->
        CaptureSettingsUiState(
            accessGranted = granted,
            totalMessages = count,
            lastCapturedAt = last,
            keepAliveEnabled = keepAlive,
            batteryExempt = exempt,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CaptureSettingsUiState(
            accessGranted = accessHelper.isAccessGranted(),
            batteryExempt = batteryHelper.isIgnoringBatteryOptimizations(),
        ),
    )

    /** Re-check OS-level state; call when returning from a system settings screen. */
    fun refreshStatus() {
        accessGranted.value = accessHelper.isAccessGranted()
        batteryExempt.value = batteryHelper.isIgnoringBatteryOptimizations()
    }

    fun setKeepAlive(enabled: Boolean) {
        viewModelScope.launch {
            keepAlivePreferences.setEnabled(enabled)
            if (enabled) serviceController.start() else serviceController.stop()
        }
    }

    fun settingsIntent(): Intent = accessHelper.settingsIntent()

    fun batteryRequestIntent(): Intent = batteryHelper.requestIntent()

    fun resolveAutostart(): AutostartHelper.Result = autostartHelper.resolve()
}
