package com.tiswu.sagex.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarTest {

    @Test
    fun `initial is first non-blank char uppercased`() {
        assertEquals("张", avatarInitial("张三"))
        assertEquals("A", avatarInitial("alice"))
        assertEquals("X", avatarInitial("   x后面"))
    }

    @Test
    fun `blank name yields question mark`() {
        assertEquals("?", avatarInitial(""))
        assertEquals("?", avatarInitial("   "))
    }

    @Test
    fun `color is deterministic per name`() {
        assertEquals(avatarColor("开发群"), avatarColor("开发群"))
        assertEquals(avatarColor("李四"), avatarColor("李四"))
    }

    @Test
    fun `blank name color is stable`() {
        assertEquals(avatarColor(""), avatarColor("   "))
    }
}
