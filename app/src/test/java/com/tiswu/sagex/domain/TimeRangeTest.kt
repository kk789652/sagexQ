package com.tiswu.sagex.domain

import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId

class TimeRangeTest {

    private val utc = ZoneId.of("UTC")
    private val now = 1_700_000_000_000L // 2023-11-14T22:13:20Z
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

    private val messages = listOf(msg(now), msg(now - hour), msg(now - day), msg(now - 10 * day))

    @Test
    fun `all keeps everything`() {
        assertEquals(4, filterByRange(messages, TimeRange.All, now, utc).size)
    }

    @Test
    fun `today keeps since UTC midnight`() {
        assertEquals(listOf(now, now - hour), filterByRange(messages, TimeRange.Today, now, utc).map { it.postedAt })
    }

    @Test
    fun `last 7 days excludes older`() {
        assertEquals(
            listOf(now, now - hour, now - day),
            filterByRange(messages, TimeRange.Last7Days, now, utc).map { it.postedAt },
        )
    }

    @Test
    fun `last 30 days keeps the 10-day-old message`() {
        assertEquals(4, filterByRange(messages, TimeRange.Last30Days, now, utc).size)
    }

    @Test
    fun `custom range is start-inclusive end-exclusive`() {
        val range = TimeRange.Custom(startMillis = now - day, endMillis = now)
        // [now-day, now): includes now-day and now-hour, excludes now
        assertEquals(listOf(now - hour, now - day), filterByRange(messages, range, now, utc).map { it.postedAt })
    }

    @Test
    fun `all bounds cover everything`() {
        assertEquals(0L to Long.MAX_VALUE, TimeRange.All.bounds(now, utc))
    }
}
