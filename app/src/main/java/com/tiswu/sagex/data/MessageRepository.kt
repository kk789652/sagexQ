package com.tiswu.sagex.data

import android.util.Log
import com.tiswu.sagex.data.local.MessageDao
import com.tiswu.sagex.data.local.toDomain
import com.tiswu.sagex.data.local.toEntity
import com.tiswu.sagex.data.sync.FeishuSyncClient
import com.tiswu.sagex.di.ApplicationScope
import com.tiswu.sagex.di.IoDispatcher
import com.tiswu.sagex.domain.TimeRange
import com.tiswu.sagex.domain.bounds
import com.tiswu.sagex.domain.model.AppSummary
import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationSummary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val dao: MessageDao,
    private val feishuSync: FeishuSyncClient,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    val totalCount: Flow<Int> = dao.observeCount()

    val lastPostedAt: Flow<Long?> = dao.observeLastPostedAt()

    val apps: Flow<List<AppSummary>> =
        dao.observeApps()
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    val conversations: Flow<List<ConversationSummary>> =
        dao.observeConversations()
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    fun conversations(sourcePackage: String): Flow<List<ConversationSummary>> =
        dao.observeConversations(sourcePackage)
            .map { rows -> rows.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    fun messages(conversationKey: String): Flow<List<CapturedMessage>> =
        dao.observeByConversation(conversationKey)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    suspend fun save(message: CapturedMessage): Long = withContext(ioDispatcher) {
        val entity = message.toEntity()
        val id = dao.insert(entity)
        applicationScope.launch {
            runCatching { feishuSync.syncMessage(entity.copy(id = id).toDomain()) }
                .onSuccess { dao.markSynced(id) }
                .onFailure { Log.w(TAG, "Failed to sync message to Feishu", it) }
        }
        id
    }

    suspend fun syncUnsynced(): Int = withContext(ioDispatcher) {
        var count = 0
        dao.unsyncedMessages().forEach { entity ->
            runCatching { feishuSync.syncMessage(entity.toDomain()) }
                .onSuccess {
                    dao.markSynced(entity.id)
                    count++
                }
                .onFailure { Log.w(TAG, "Manual Feishu sync failed for message ${entity.id}", it) }
        }
        count
    }

    suspend fun count(): Int = withContext(ioDispatcher) { dao.count() }

    suspend fun clearAll() = withContext(ioDispatcher) { dao.clearAll() }

    suspend fun deleteMessage(id: Long) = withContext(ioDispatcher) {
        dao.deleteById(id)
    }

    suspend fun deleteConversation(conversationKey: String) = withContext(ioDispatcher) {
        dao.deleteInRange(conversationKey, Long.MIN_VALUE, Long.MAX_VALUE)
    }

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

    private companion object {
        const val TAG = "MessageRepository"
    }
}
