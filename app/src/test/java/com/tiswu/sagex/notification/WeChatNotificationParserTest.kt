package com.tiswu.sagex.notification

import com.tiswu.sagex.domain.ParseResult
import com.tiswu.sagex.domain.RawNotification
import com.tiswu.sagex.domain.model.ConversationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeChatNotificationParserTest {

    private val parser = WeChatNotificationParser()
    private val wechat = WeChatNotificationParser.WECHAT_PACKAGE

    private fun raw(
        pkg: String = wechat,
        title: String? = null,
        text: String? = null,
        lines: List<String> = emptyList(),
        ticker: String? = null,
        postedAt: Long = 1_000L,
    ) = RawNotification(pkg, title, text, lines, ticker, postedAt)

    @Test
    fun `supports only wechat`() {
        assertTrue(parser.supports(wechat))
        assertTrue(!parser.supports("com.other.app"))
    }

    @Test
    fun `group message splits sender and content`() {
        val result = parser.parse(raw(title = "开发群", text = "张三: 今天要发布版本"))
        result as ParseResult.Parsed
        assertEquals(ConversationType.GROUP, result.message.conversationType)
        assertEquals("开发群", result.message.conversationTitle)
        assertEquals("张三", result.message.sender)
        assertEquals("今天要发布版本", result.message.body)
    }

    @Test
    fun `group message with fullwidth colon`() {
        val result = parser.parse(raw(title = "产品群", text = "王五：晚点开会"))
        result as ParseResult.Parsed
        assertEquals(ConversationType.GROUP, result.message.conversationType)
        assertEquals("王五", result.message.sender)
        assertEquals("晚点开会", result.message.body)
    }

    @Test
    fun `english group message`() {
        val result = parser.parse(raw(title = "Team", text = "Bob: deploying now"))
        result as ParseResult.Parsed
        assertEquals(ConversationType.GROUP, result.message.conversationType)
        assertEquals("Bob", result.message.sender)
        assertEquals("deploying now", result.message.body)
    }

    @Test
    fun `direct message has no sender prefix`() {
        val result = parser.parse(raw(title = "李四", text = "在吗"))
        result as ParseResult.Parsed
        assertEquals(ConversationType.DIRECT, result.message.conversationType)
        assertEquals("李四", result.message.conversationTitle)
        assertEquals("李四", result.message.sender)
        assertEquals("在吗", result.message.body)
    }

    @Test
    fun `aggregated chinese notification is content unavailable`() {
        val result = parser.parse(raw(title = "微信", text = "2条新消息"))
        assertTrue(result is ParseResult.ContentUnavailable)
    }

    @Test
    fun `count-prefixed group message is parsed`() {
        val result = parser.parse(raw(title = "开发群", text = "[3条]张三: 你好"))
        result as ParseResult.Parsed
        assertEquals(ConversationType.GROUP, result.message.conversationType)
        assertEquals("开发群", result.message.conversationTitle)
        assertEquals("张三", result.message.sender)
        assertEquals("你好", result.message.body)
    }

    @Test
    fun `count-prefixed direct message is parsed`() {
        val result = parser.parse(raw(title = "李四", text = "[2条]在吗"))
        result as ParseResult.Parsed
        assertEquals(ConversationType.DIRECT, result.message.conversationType)
        assertEquals("李四", result.message.sender)
        assertEquals("在吗", result.message.body)
    }

    @Test
    fun `bare count prefix without 条 is stripped from group message`() {
        // WeChat also emits "[82]" with no 条; that leaked into sender as "[82]一条鱼".
        val result = parser.parse(raw(title = "开发群", text = "[82]一条鱼: 融资买杠杆"))
        result as ParseResult.Parsed
        assertEquals(ConversationType.GROUP, result.message.conversationType)
        assertEquals("一条鱼", result.message.sender)
        assertEquals("融资买杠杆", result.message.body)
    }

    @Test
    fun `bare count prefix without 条 is stripped from direct message`() {
        val result = parser.parse(raw(title = "李四", text = "[12]在吗"))
        result as ParseResult.Parsed
        assertEquals(ConversationType.DIRECT, result.message.conversationType)
        assertEquals("李四", result.message.sender)
        assertEquals("在吗", result.message.body)
    }

    @Test
    fun `bracketed emoji nickname is not mistaken for a count prefix`() {
        // "[鲸鱼]" / "[火箭]Steve." are real display names — stripping them would
        // destroy far more rows than the count-prefix bug it is meant to fix.
        val result = parser.parse(raw(title = "开发群", text = "[火箭]Steve.: 发车了"))
        result as ParseResult.Parsed
        assertEquals("[火箭]Steve.", result.message.sender)
        assertEquals("发车了", result.message.body)
    }

    @Test
    fun `bracketed emoji only nickname survives in direct message`() {
        val result = parser.parse(raw(title = "[鲸鱼]", text = "在吗"))
        result as ParseResult.Parsed
        assertEquals("[鲸鱼]", result.message.sender)
        assertEquals("在吗", result.message.body)
    }

    @Test
    fun `fullwidth bracket count prefix is stripped`() {
        val result = parser.parse(raw(title = "产品群", text = "【5条】王五: 看一下"))
        result as ParseResult.Parsed
        assertEquals(ConversationType.GROUP, result.message.conversationType)
        assertEquals("王五", result.message.sender)
        assertEquals("看一下", result.message.body)
    }

    @Test
    fun `pure summary count remains content unavailable`() {
        assertTrue(parser.parse(raw(title = "微信", text = "5条消息来自3个聊天")) is ParseResult.ContentUnavailable)
        assertTrue(parser.parse(raw(title = "微信", text = "2条新消息")) is ParseResult.ContentUnavailable)
    }

    @Test
    fun `bracketed multi-chat summary stays content unavailable`() {
        val result = parser.parse(raw(title = "微信", text = "[3条]来自2个聊天"))
        assertTrue(result is ParseResult.ContentUnavailable)
    }

    @Test
    fun `bare number-leading content is not treated as a count prefix`() {
        val result = parser.parse(raw(title = "李四", text = "3条建议给你"))
        result as ParseResult.Parsed
        assertEquals(ConversationType.DIRECT, result.message.conversationType)
        assertEquals("3条建议给你", result.message.body)
    }

    @Test
    fun `aggregated english notification is content unavailable`() {
        val result = parser.parse(raw(title = "WeChat", text = "3 new messages"))
        assertTrue(result is ParseResult.ContentUnavailable)
    }

    @Test
    fun `empty notification is ignored`() {
        assertEquals(ParseResult.Ignored, parser.parse(raw(title = null, text = null)))
        assertEquals(ParseResult.Ignored, parser.parse(raw(title = "李四", text = "")))
    }

    @Test
    fun `non-wechat notification is ignored`() {
        val result = parser.parse(raw(pkg = "com.other.app", title = "x", text = "y: z"))
        assertEquals(ParseResult.Ignored, result)
    }

    @Test
    fun `falls back to lines when text missing`() {
        val result = parser.parse(raw(title = "群A", text = null, lines = listOf("旧消息", "钱七: 看一下")))
        result as ParseResult.Parsed
        assertEquals("钱七", result.message.sender)
        assertEquals("看一下", result.message.body)
    }

    @Test
    fun `preserves posted timestamp`() {
        val result = parser.parse(raw(title = "李四", text = "hi", postedAt = 42L))
        result as ParseResult.Parsed
        assertEquals(42L, result.message.postedAt)
    }
}
