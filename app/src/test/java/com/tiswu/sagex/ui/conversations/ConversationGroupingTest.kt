package com.tiswu.sagex.ui.conversations

import com.tiswu.sagex.domain.model.ConversationSummary
import com.tiswu.sagex.domain.model.ConversationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationGroupingTest {

    private fun conv(type: ConversationType, title: String, last: Long) = ConversationSummary(
        conversationKey = "key-$title",
        conversationTitle = title,
        conversationType = type,
        messageCount = 1,
        lastPostedAt = last,
    )

    @Test
    fun `separates groups and directs into ordered sections`() {
        val input = listOf(
            conv(ConversationType.DIRECT, "李四", 300),
            conv(ConversationType.GROUP, "开发群", 200),
            conv(ConversationType.DIRECT, "张三", 100),
        )

        val sections = ConversationGrouping.intoSections(input)

        assertEquals(listOf(ConversationType.GROUP, ConversationType.DIRECT), sections.map { it.type })
        assertEquals(listOf("开发群"), sections[0].items.map { it.conversationTitle })
        assertEquals(listOf("李四", "张三"), sections[1].items.map { it.conversationTitle })
    }

    @Test
    fun `drops empty sections`() {
        val input = listOf(conv(ConversationType.GROUP, "群A", 10))
        val sections = ConversationGrouping.intoSections(input)
        assertEquals(1, sections.size)
        assertEquals(ConversationType.GROUP, sections.single().type)
    }

    @Test
    fun `preserves input recency order within a section`() {
        val input = listOf(
            conv(ConversationType.GROUP, "新", 300),
            conv(ConversationType.GROUP, "中", 200),
            conv(ConversationType.GROUP, "旧", 100),
        )
        val items = ConversationGrouping.intoSections(input).single().items
        assertEquals(listOf("新", "中", "旧"), items.map { it.conversationTitle })
    }

    @Test
    fun `unknown type goes last`() {
        val input = listOf(
            conv(ConversationType.UNKNOWN, "未知", 300),
            conv(ConversationType.GROUP, "群", 200),
            conv(ConversationType.DIRECT, "人", 100),
        )
        assertEquals(
            listOf(ConversationType.GROUP, ConversationType.DIRECT, ConversationType.UNKNOWN),
            ConversationGrouping.intoSections(input).map { it.type },
        )
    }

    @Test
    fun `empty input yields no sections`() {
        assertTrue(ConversationGrouping.intoSections(emptyList()).isEmpty())
    }
}
