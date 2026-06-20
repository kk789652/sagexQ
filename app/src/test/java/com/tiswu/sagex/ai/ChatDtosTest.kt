package com.tiswu.sagex.ai

import com.tiswu.sagex.data.prefs.ApiConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatDtosTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `isConfigured requires all three fields`() {
        assertTrue(ApiConfig("https://x/v1", "key", "gpt-4o-mini").isConfigured)
        assertFalse(ApiConfig("", "key", "model").isConfigured)
        assertFalse(ApiConfig("https://x", "", "model").isConfigured)
        assertFalse(ApiConfig("https://x", "key", "").isConfigured)
        assertFalse(ApiConfig().isConfigured)
    }

    @Test
    fun `parses content from a standard chat completion response`() {
        val body = """
            {
              "id": "chatcmpl-1",
              "object": "chat.completion",
              "choices": [
                { "index": 0, "message": { "role": "assistant", "content": "总结内容" }, "finish_reason": "stop" }
              ],
              "usage": { "total_tokens": 42 }
            }
        """.trimIndent()
        val parsed = json.decodeFromString<ChatCompletionResponse>(body)
        assertEquals("总结内容", parsed.firstContent())
    }

    @Test
    fun `missing choices yields null content`() {
        val parsed = json.decodeFromString<ChatCompletionResponse>("""{"id":"x","choices":[]}""")
        assertNull(parsed.firstContent())
    }

    @Test
    fun `blank content yields null`() {
        val body = """{"choices":[{"message":{"role":"assistant","content":"   "}}]}"""
        val parsed = json.decodeFromString<ChatCompletionResponse>(body)
        assertNull(parsed.firstContent())
    }
}
