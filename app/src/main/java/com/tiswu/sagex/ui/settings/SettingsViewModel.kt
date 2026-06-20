package com.tiswu.sagex.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiswu.sagex.data.prefs.ApiConfig
import com.tiswu.sagex.data.prefs.ApiSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiSettings: ApiSettings,
) : ViewModel() {

    val config: StateFlow<ApiConfig> = apiSettings.config.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ApiConfig(),
    )

    fun save(baseUrl: String, apiKey: String, model: String) {
        viewModelScope.launch {
            apiSettings.save(ApiConfig(baseUrl = baseUrl, apiKey = apiKey, model = model))
        }
    }
}
