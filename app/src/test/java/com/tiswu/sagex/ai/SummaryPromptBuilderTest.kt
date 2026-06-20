package com.tiswu.sagex.ai

import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class SummaryPromptBuilderTest {

    private val utc = ZoneId.of("UTC")

    private fun msg(body: String, postedAt: Long = 1_700_000_000_000L) = CapturedMessage(
        sourcePackage = "com.tencent.mm",
        conversationKey = "k",
        conversationType = ConversationType.GROUP,
        conversationTitle = "群",
        sender = "张三",
        body = body,
        postedAt = postedAt,
    )

    @Test
    fun `empty messages yield no prompt`() {
        assertTrue(SummaryPromptBuilder.build("群", emptyList()).isEmpty())
    }

    @Test
    fun `produces system and user messages`() {
        val prompt = SummaryPromptBuilder.build("群", listOf(msg("你好")), zone = utc)
        assertEquals(2, prompt.size)
        assertEquals("system", prompt[0].role)
        assertEquals("user", prompt[1].role)
        assertTrue(prompt[1].content.contains("张三: 你好"))
        assertTrue(prompt[1].content.contains("群"))
    }

    @Test
    fun `caps to the most recent messages`() {
        val messages = (1..5).map { msg("m$it", postedAt = 1_700_000_000_000L + it) }
        val prompt = SummaryPromptBuilder.build("群", messages, max = 2, zone = utc)
        val user = prompt[1].content
        assertTrue(user.contains("m4"))
        assertTrue(user.contains("m5"))
        assertTrue(!user.contains("m1"))
        assertTrue(!user.contains("m3"))
    }
}
