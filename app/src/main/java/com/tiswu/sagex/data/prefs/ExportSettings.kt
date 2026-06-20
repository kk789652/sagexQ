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

/** Persists the instruction header prepended to exported/shared transcripts. */
@Singleton
class ExportSettings @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val instructionHeader: Flow<String> = dataStore.data.map { it[KEY] ?: DEFAULT_INSTRUCTION }

    suspend fun current(): String = instructionHeader.first()

    suspend fun setInstructionHeader(value: String) {
        dataStore.edit { it[KEY] = value }
    }

    companion object {
        const val DEFAULT_INSTRUCTION = "请总结以下对话,提炼要点、待办事项和需要关注的重要信息:"
        private val KEY = stringPreferencesKey("export_instruction_header")
    }
}
