package com.tiswu.sagex.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.tiswu.sagex.domain.ConversationKey
import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MessageDaoTest {

    private lateinit var db: SageXDatabase
    private lateinit var dao: MessageDao

    private val pkg = "com.tencent.mm"

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SageXDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = db.messageDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun message(
        type: ConversationType,
        title: String,
        sender: String?,
        body: String,
        postedAt: Long,
    ) = CapturedMessage(
        sourcePackage = pkg,
        conversationKey = ConversationKey.of(pkg, type, title),
        conversationType = type,
        conversationTitle = title,
        sender = sender,
        body = body,
        postedAt = postedAt,
    ).toEntity()

    @Test
    fun `insert and retrieve by conversation`() = runTest {
        val msg = message(ConversationType.DIRECT, "李四", "李四", "在吗", 100L)
        dao.insert(msg)

        val stored = dao.byConversation(msg.conversationKey)
        assertEquals(1, stored.size)
        assertEquals("在吗", stored.first().body)
    }

    @Test
    fun `messages returned in chronological order`() = runTest {
        val key = ConversationKey.of(pkg, ConversationType.GROUP, "群A")
        dao.insert(message(ConversationType.GROUP, "群A", "b", "second", 200L))
        dao.insert(message(ConversationType.GROUP, "群A", "a", "first", 100L))

        val stored = dao.byConversation(key)
        assertEquals(listOf("first", "second"), stored.map { it.body })
    }

    @Test
    fun `count reflects inserts`() = runTest {
        dao.insert(message(ConversationType.DIRECT, "A", "A", "1", 1L))
        dao.insert(message(ConversationType.DIRECT, "B", "B", "2", 2L))
        dao.insert(message(ConversationType.DIRECT, "C", "C", "3", 3L))

        assertEquals(3, dao.count())
        assertEquals(3, dao.observeCount().first())
    }

    @Test
    fun `clearAll empties the table`() = runTest {
        dao.insert(message(ConversationType.DIRECT, "A", "A", "1", 1L))
        dao.clearAll()
        assertEquals(0, dao.count())
    }

    @Test
    fun `conversations aggregate groups by key`() = runTest {
        dao.insert(message(ConversationType.GROUP, "群A", "a", "1", 100L))
        dao.insert(message(ConversationType.GROUP, "群A", "b", "2", 300L))
        dao.insert(message(ConversationType.DIRECT, "李四", "李四", "hi", 200L))

        val conversations = dao.observeConversations().first()
        assertEquals(2, conversations.size)
        // ordered by lastPostedAt desc -> 群A (300) first
        val groupA = conversations.first()
        assertEquals("群A", groupA.conversationTitle)
        assertEquals(2, groupA.messageCount)
        assertEquals(300L, groupA.lastPostedAt)
    }

    @Test
    fun `group and direct with same title stay distinct`() = runTest {
        dao.insert(message(ConversationType.GROUP, "张三", "x", "group msg", 100L))
        dao.insert(message(ConversationType.DIRECT, "张三", "张三", "direct msg", 200L))

        val conversations = dao.observeConversations().first()
        assertEquals(2, conversations.size)
    }
}
