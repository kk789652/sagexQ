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
        sourcePackage: String = pkg,
    ) = CapturedMessage(
        sourcePackage = sourcePackage,
        conversationKey = ConversationKey.of(sourcePackage, type, title),
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
    fun `deleteInRange removes only in-range messages`() = runTest {
        val key = ConversationKey.of(pkg, ConversationType.DIRECT, "A")
        dao.insert(message(ConversationType.DIRECT, "A", "A", "old", 100L))
        dao.insert(message(ConversationType.DIRECT, "A", "A", "mid", 200L))
        dao.insert(message(ConversationType.DIRECT, "A", "A", "new", 300L))

        dao.deleteInRange(150L, 250L) // removes only "mid"

        assertEquals(listOf("old", "new"), dao.byConversation(key).map { it.body })
    }

    @Test
    fun `per-conversation deleteInRange leaves other conversations`() = runTest {
        val groupKey = ConversationKey.of(pkg, ConversationType.GROUP, "群")
        val directKey = ConversationKey.of(pkg, ConversationType.DIRECT, "李四")
        dao.insert(message(ConversationType.GROUP, "群", "x", "g", 100L))
        dao.insert(message(ConversationType.DIRECT, "李四", "李四", "d", 100L))

        dao.deleteInRange(groupKey, 0L, Long.MAX_VALUE)

        assertEquals(0, dao.byConversation(groupKey).size)
        assertEquals(1, dao.byConversation(directKey).size)
    }

    @Test
    fun `conversations expose the latest message body and sender`() = runTest {
        dao.insert(message(ConversationType.GROUP, "群A", "甲", "早", 100L))
        dao.insert(message(ConversationType.GROUP, "群A", "乙", "最新一条", 300L))
        dao.insert(message(ConversationType.GROUP, "群A", "丙", "中间", 200L))

        val summary = dao.observeConversations().first().single()
        assertEquals("最新一条", summary.lastMessage)
        assertEquals("乙", summary.lastSender)
    }

    @Test
    fun `group and direct with same title stay distinct`() = runTest {
        dao.insert(message(ConversationType.GROUP, "张三", "x", "group msg", 100L))
        dao.insert(message(ConversationType.DIRECT, "张三", "张三", "direct msg", 200L))

        val conversations = dao.observeConversations().first()
        assertEquals(2, conversations.size)
    }

    @Test
    fun `apps aggregate groups by source package`() = runTest {
        val otherPkg = "com.example.chat"
        dao.insert(message(ConversationType.GROUP, "群A", "a", "1", 100L))
        dao.insert(message(ConversationType.GROUP, "群A", "b", "2", 300L))
        dao.insert(message(ConversationType.DIRECT, "李四", "李四", "hi", 200L))
        dao.insert(
            message(
                ConversationType.DIRECT,
                "Other",
                "Other",
                "other",
                400L,
                sourcePackage = otherPkg,
            ),
        )

        val apps = dao.observeApps().first()
        assertEquals(2, apps.size)

        val other = apps.first()
        assertEquals(otherPkg, other.sourcePackage)
        assertEquals(1, other.conversationCount)
        assertEquals(1, other.messageCount)
        assertEquals(400L, other.lastPostedAt)

        val wechat = apps[1]
        assertEquals(pkg, wechat.sourcePackage)
        assertEquals(2, wechat.conversationCount)
        assertEquals(3, wechat.messageCount)
        assertEquals(300L, wechat.lastPostedAt)
    }

    @Test
    fun `scoped conversations include only requested source package`() = runTest {
        val otherPkg = "com.example.chat"
        dao.insert(message(ConversationType.DIRECT, "A", "A", "wechat", 100L))
        dao.insert(message(ConversationType.GROUP, "群", "B", "wechat group", 200L))
        dao.insert(
            message(
                ConversationType.DIRECT,
                "A",
                "A",
                "other",
                300L,
                sourcePackage = otherPkg,
            ),
        )

        val conversations = dao.observeConversations(pkg).first()
        assertEquals(2, conversations.size)
        assertEquals(listOf("群", "A"), conversations.map { it.conversationTitle })
        assertEquals(listOf("wechat group", "wechat"), conversations.map { it.lastMessage })

        val otherConversations = dao.observeConversations(otherPkg).first()
        assertEquals(1, otherConversations.size)
        assertEquals("other", otherConversations.single().lastMessage)
    }
}
