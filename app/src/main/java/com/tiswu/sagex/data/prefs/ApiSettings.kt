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

/** Configuration for an OpenAI-compatible API. */
data class ApiConfig(
    val baseUrl: String = "",
    val apiKey: String = "",
    val model: String = "",
) {
    /** Configured only when all three fields are non-blank. */
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && apiKey.isNotBlank() && model.isNotBlank()
}

/** Persists the OpenAI-compatible API configuration. */
@Singleton
class ApiSettings @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    val config: Flow<ApiConfig> = dataStore.data.map { prefs ->
        ApiConfig(
            baseUrl = prefs[BASE_URL].orEmpty(),
            apiKey = prefs[API_KEY].orEmpty(),
            model = prefs[MODEL].orEmpty(),
        )
    }

    suspend fun current(): ApiConfig = config.first()

    suspend fun save(config: ApiConfig) {
        dataStore.edit { prefs ->
            prefs[BASE_URL] = config.baseUrl.trim().trimEnd('/')
            prefs[API_KEY] = config.apiKey.trim()
            prefs[MODEL] = config.model.trim()
        }
    }

    companion object {
        private val BASE_URL = stringPreferencesKey("api_base_url")
        private val API_KEY = stringPreferencesKey("api_key")
        private val MODEL = stringPreferencesKey("api_model")
    }
}
