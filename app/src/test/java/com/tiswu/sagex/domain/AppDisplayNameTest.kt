package com.tiswu.sagex.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AppDisplayNameTest {

    @Test
    fun `wechat package maps to Chinese display name`() {
        assertEquals("微信", appDisplayName("com.tencent.mm"))
    }

    @Test
    fun `unknown package maps to itself`() {
        assertEquals("com.example.chat", appDisplayName("com.example.chat"))
    }
}
