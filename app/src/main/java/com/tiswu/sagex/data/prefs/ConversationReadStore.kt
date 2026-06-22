package com.tiswu.sagex.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remembers where the user left off in each conversation so reopening a chat restores the last
 * read position instead of always starting at the oldest message. Keyed by conversationKey,
 * stores the id of the message that was at the top of the viewport.
 */
@Singleton
class ConversationReadStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    /** The last-read message id for [conversationKey], or null if none has been recorded yet. */
    suspend fun lastReadId(conversationKey: String): Long? =
        dataStore.data.map { it[keyFor(conversationKey)] }.first()

    suspend fun setLastReadId(conversationKey: String, messageId: Long) {
        dataStore.edit { it[keyFor(conversationKey)] = messageId }
    }

    private companion object {
        fun keyFor(conversationKey: String) = longPreferencesKey("read_pos_$conversationKey")
    }
}
