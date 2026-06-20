package com.tiswu.sagex.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Persists the user's "keep capture alive" choice. */
@Singleton
class KeepAlivePreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val enabled: Flow<Boolean> = dataStore.data.map { it[KEY] ?: false }

    suspend fun isEnabled(): Boolean = enabled.first()

    suspend fun setEnabled(value: Boolean) {
        dataStore.edit { it[KEY] = value }
    }

    companion object {
        private val KEY = booleanPreferencesKey("keep_alive_enabled")
    }
}
