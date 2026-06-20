package com.tiswu.sagex.domain

import com.tiswu.sagex.domain.model.ConversationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ConversationKeyTest {

    private val pkg = "com.tencent.mm"

    @Test
    fun `same conversation produces same key`() {
        val a = ConversationKey.of(pkg, ConversationType.GROUP, "开发群")
        val b = ConversationKey.of(pkg, ConversationType.GROUP, "开发群")
        assertEquals(a, b)
    }

    @Test
    fun `group and direct with same title are distinct`() {
        val group = ConversationKey.of(pkg, ConversationType.GROUP, "张三")
        val direct = ConversationKey.of(pkg, ConversationType.DIRECT, "张三")
        assertNotEquals(group, direct)
    }

    @Test
    fun `different titles are distinct`() {
        val a = ConversationKey.of(pkg, ConversationType.DIRECT, "张三")
        val b = ConversationKey.of(pkg, ConversationType.DIRECT, "李四")
        assertNotEquals(a, b)
    }

    @Test
    fun `different source packages are distinct`() {
        val a = ConversationKey.of("com.tencent.mm", ConversationType.GROUP, "群")
        val b = ConversationKey.of("com.other.app", ConversationType.GROUP, "群")
        assertNotEquals(a, b)
    }

    @Test
    fun `key is stable hex of fixed length`() {
        val key = ConversationKey.of(pkg, ConversationType.GROUP, "群")
        assertEquals(64, key.length) // SHA-256 hex
    }
}
