package com.tiswu.sagex.ai

import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class TranscriptExporterTest {

    private val utc = ZoneId.of("UTC")

    private fun msg(body: String, sender: String = "张三", postedAt: Long = 1_700_000_000_000L) =
        CapturedMessage(
            sourcePackage = "com.tencent.mm",
            conversationKey = "k",
            conversationType = ConversationType.GROUP,
            conversationTitle = "群",
            sender = sender,
            body = body,
            postedAt = postedAt,
        )

    @Test
    fun `includes instruction header when present`() {
        val text = TranscriptExporter.export("群", listOf(msg("你好")), "请总结:", zone = utc)
        assertTrue(text.startsWith("请总结:"))
        assertTrue(text.contains("张三: 你好"))
        assertTrue(text.contains("会话:群"))
    }

    @Test
    fun `omits header when blank`() {
        val text = TranscriptExporter.export("群", listOf(msg("你好")), "", zone = utc)
        assertTrue(text.startsWith("会话:群"))
    }

    @Test
    fun `caps to most recent messages`() {
        val messages = (1..5).map { msg("m$it", postedAt = 1_700_000_000_000L + it) }
        val text = TranscriptExporter.export("群", messages, "", max = 2, zone = utc)
        assertTrue(text.contains("m4"))
        assertTrue(text.contains("m5"))
        assertTrue(!text.contains("m1"))
        assertTrue(!text.contains("m3"))
    }

    @Test
    fun `empty messages still produces header and title`() {
        val text = TranscriptExporter.export("群", emptyList(), "请总结:", zone = utc)
        assertTrue(text.contains("请总结:"))
        assertTrue(text.contains("会话:群"))
    }

    @Test
    fun `unknown sender falls back to question mark`() {
        val text = TranscriptExporter.export("群", listOf(msg("hi", sender = "")), "", zone = utc)
        assertEquals(true, text.contains("?: hi"))
    }
}
