package com.tiswu.sagex.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Persists the prompt used for summaries and exports, keyed by source app package. */
@Singleton
class PromptSettings @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    fun promptFor(pkg: String): Flow<String> =
        dataStore.data.map { it[keyFor(pkg)] ?: DEFAULT_PROMPT }

    suspend fun currentPrompt(pkg: String): String = promptFor(pkg).first()

    suspend fun setPrompt(pkg: String, value: String) {
        dataStore.edit { it[keyFor(pkg)] = value }
    }

    companion object {
        const val DEFAULT_PROMPT =
            "你是一个聊天记录总结助手。请用简洁的中文总结以下聊天内容,提炼要点、待办事项和需要关注的重要信息。" +
                "如果有人提出问题或请求,请单独列出。"

        private fun keyFor(pkg: String) = stringPreferencesKey("prompt_$pkg")
    }
}
