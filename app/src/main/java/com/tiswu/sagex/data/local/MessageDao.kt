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

    @Query("UPDATE messages SET feishu_synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("SELECT * FROM messages WHERE feishu_synced = 0 ORDER BY postedAt ASC")
    suspend fun unsyncedMessages(): List<MessageEntity>

    @Query(
        """
        SELECT sourcePackage AS sourcePackage,
               COUNT(DISTINCT conversationKey) AS conversationCount,
               COUNT(*) AS messageCount,
               MAX(postedAt) AS lastPostedAt
        FROM messages
        GROUP BY sourcePackage
        ORDER BY lastPostedAt DESC
        """,
    )
    fun observeApps(): Flow<List<AppSummaryRow>>

    @Query(
        """
        SELECT m.conversationKey AS conversationKey,
               m.conversationTitle AS conversationTitle,
               m.conversationType AS conversationType,
               COUNT(*) AS messageCount,
               MAX(m.postedAt) AS lastPostedAt,
               (SELECT body FROM messages x WHERE x.conversationKey = m.conversationKey
                   ORDER BY x.postedAt DESC, x.id DESC LIMIT 1) AS lastMessage,
               (SELECT sender FROM messages x WHERE x.conversationKey = m.conversationKey
                   ORDER BY x.postedAt DESC, x.id DESC LIMIT 1) AS lastSender
        FROM messages m
        GROUP BY m.conversationKey
        ORDER BY lastPostedAt DESC
        """,
    )
    fun observeConversations(): Flow<List<ConversationSummaryRow>>

    @Query(
        """
        SELECT m.conversationKey AS conversationKey,
               m.conversationTitle AS conversationTitle,
               m.conversationType AS conversationType,
               COUNT(*) AS messageCount,
               MAX(m.postedAt) AS lastPostedAt,
               (SELECT body FROM messages x
                   WHERE x.sourcePackage = :sourcePackage
                     AND x.conversationKey = m.conversationKey
                   ORDER BY x.postedAt DESC, x.id DESC LIMIT 1) AS lastMessage,
               (SELECT sender FROM messages x
                   WHERE x.sourcePackage = :sourcePackage
                     AND x.conversationKey = m.conversationKey
                   ORDER BY x.postedAt DESC, x.id DESC LIMIT 1) AS lastSender
        FROM messages m
        WHERE m.sourcePackage = :sourcePackage
        GROUP BY m.conversationKey
        ORDER BY lastPostedAt DESC
        """,
    )
    fun observeConversations(sourcePackage: String): Flow<List<ConversationSummaryRow>>

    @Query("DELETE FROM messages")
    suspend fun clearAll()

    @Query("DELETE FROM messages WHERE postedAt >= :start AND postedAt < :end")
    suspend fun deleteInRange(start: Long, end: Long)

    @Query("DELETE FROM messages WHERE conversationKey = :conversationKey AND postedAt >= :start AND postedAt < :end")
    suspend fun deleteInRange(conversationKey: String, start: Long, end: Long)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteById(id: Long)
}
