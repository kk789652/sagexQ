package com.tiswu.sagex.ui.summary

import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SummarySelectionTest {

    @Test
    fun `selectMessages returns all selected candidates`() {
        val messages = listOf(message(1), message(2), message(3))

        val selected = selectMessages(messages, setOf(1, 2, 3))

        assertEquals(messages, selected)
    }

    @Test
    fun `selectMessages returns selected subset in candidate order`() {
        val messages = listOf(message(1), message(2), message(3))

        val selected = selectMessages(messages, setOf(3, 1))

        assertEquals(listOf(messages[0], messages[2]), selected)
    }

    @Test
    fun `selectMessages returns empty when no ids are selected`() {
        val messages = listOf(message(1), message(2), message(3))

        val selected = selectMessages(messages, emptySet())

        assertTrue(selected.isEmpty())
    }

    private fun message(id: Long) = CapturedMessage(
        id = id,
        sourcePackage = "com.tencent.mm",
        conversationKey = "conversation",
        conversationType = ConversationType.GROUP,
        conversationTitle = "群",
        sender = "张三",
        body = "message $id",
        postedAt = 1_700_000_000_000L + id,
    )
}
