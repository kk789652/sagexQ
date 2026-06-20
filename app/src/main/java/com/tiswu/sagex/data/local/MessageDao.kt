package com.tiswu.sagex.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Insert
    suspend fun insert(message: MessageEntity): Long

    @Query("SELECT COUNT(*) FROM messages")
    fun observeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages")
    suspend fun count(): Int

    @Query("SELECT MAX(postedAt) FROM messages")
    fun observeLastPostedAt(): Flow<Long?>

    @Query("SELECT * FROM messages WHERE conversationKey = :conversationKey ORDER BY postedAt ASC")
    fun observeByConversation(conversationKey: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationKey = :conversationKey ORDER BY postedAt ASC")
    suspend fun byConversation(conversationKey: String): List<MessageEntity>

    @Query(
        """
        SELECT conversationKey AS conversationKey,
               conversationTitle AS conversationTitle,
               conversationType AS conversationType,
               COUNT(*) AS messageCount,
               MAX(postedAt) AS lastPostedAt
        FROM messages
        GROUP BY conversationKey
        ORDER BY lastPostedAt DESC
        """,
    )
    fun observeConversations(): Flow<List<ConversationSummaryRow>>

    @Query("DELETE FROM messages")
    suspend fun clearAll()

    @Query("DELETE FROM messages WHERE postedAt >= :start AND postedAt < :end")
    suspend fun deleteInRange(start: Long, end: Long)

    @Query("DELETE FROM messages WHERE conversationKey = :conversationKey AND postedAt >= :start AND postedAt < :end")
    suspend fun deleteInRange(conversationKey: String, start: Long, end: Long)
}
