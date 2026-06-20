package com.tiswu.sagex.data

import com.tiswu.sagex.data.local.MessageDao
import com.tiswu.sagex.data.local.toDomain
import com.tiswu.sagex.data.local.toEntity
import com.tiswu.sagex.di.IoDispatcher
import com.tiswu.sagex.domain.TimeRange
import com.tiswu.sagex.domain.bounds
import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val dao: MessageDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    val totalCount: Flow<Int> = dao.observeCount()

    val lastPostedAt: Flow<Long?> = dao.observeLastPostedAt()

    val conversations: Flow<List<ConversationSummary>> =
        dao.observeConversations().map { rows -> rows.map { it.toDomain() } }

    fun messages(conversationKey: String): Flow<List<CapturedMessage>> =
        dao.observeByConversation(conversationKey).map { list -> list.map { it.toDomain() } }

    suspend fun save(message: CapturedMessage): Long = withContext(ioDispatcher) {
        dao.insert(message.toEntity())
    }

    suspend fun count(): Int = withContext(ioDispatcher) { dao.count() }

    suspend fun clearAll() = withContext(ioDispatcher) { dao.clearAll() }

    suspend fun deleteByRange(range: TimeRange, now: Long = System.currentTimeMillis()) =
        withContext(ioDispatcher) {
            val (start, end) = range.bounds(now)
            dao.deleteInRange(start, end)
        }

    suspend fun deleteByRange(
        conversationKey: String,
        range: TimeRange,
        now: Long = System.currentTimeMillis(),
    ) = withContext(ioDispatcher) {
        val (start, end) = range.bounds(now)
        dao.deleteInRange(conversationKey, start, end)
    }
}
