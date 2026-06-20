package com.tiswu.sagex.ai

import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId

class MessageRangeTest {

    private val utc = ZoneId.of("UTC")

    // 2023-11-14T22:13:20Z
    private val now = 1_700_000_000_000L
    private val hour = 3_600_000L
    private val day = 24 * hour

    private fun msg(postedAt: Long) = CapturedMessage(
        sourcePackage = "com.tencent.mm",
        conversationKey = "k",
        conversationType = ConversationType.DIRECT,
        conversationTitle = "t",
        sender = "s",
        body = "b",
        postedAt = postedAt,
    )

    private val messages = listOf(
        msg(now),            // now
        msg(now - hour),     // earlier today (UTC)
        msg(now - day),      // yesterday
        msg(now - 10 * day), // 10 days ago
    )

    @Test
    fun `ALL keeps everything`() {
        assertEquals(4, filterByRange(messages, MessageRange.ALL, now, utc).size)
    }

    @Test
    fun `TODAY keeps only messages since UTC midnight`() {
        val result = filterByRange(messages, MessageRange.TODAY, now, utc)
        assertEquals(listOf(now, now - hour), result.map { it.postedAt })
    }

    @Test
    fun `LAST_7_DAYS excludes older than 7 days`() {
        val result = filterByRange(messages, MessageRange.LAST_7_DAYS, now, utc)
        assertEquals(listOf(now, now - hour, now - day), result.map { it.postedAt })
    }
}
