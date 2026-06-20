package com.tiswu.sagex.ui.prompt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiswu.sagex.data.MessageRepository
import com.tiswu.sagex.data.prefs.PromptSettings
import com.tiswu.sagex.domain.model.AppSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PromptConfigItem(
    val app: AppSummary,
    val prompt: String,
)

data class PromptConfigUiState(
    val items: List<PromptConfigItem> = emptyList(),
)

@HiltViewModel
class PromptConfigViewModel @Inject constructor(
    repository: MessageRepository,
    private val promptSettings: PromptSettings,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<PromptConfigUiState> = repository.apps
        .flatMapLatest { apps ->
            if (apps.isEmpty()) {
                flowOf(PromptConfigUiState())
            } else {
                combine(apps.map { promptSettings.promptFor(it.sourcePackage) }) { prompts ->
                    PromptConfigUiState(
                        items = prompts.mapIndexed { index, prompt ->
                            PromptConfigItem(app = apps[index], prompt = prompt)
                        },
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PromptConfigUiState(),
        )

    fun save(pkg: String, text: String) {
        viewModelScope.launch {
            promptSettings.setPrompt(pkg, text)
        }
    }
}
