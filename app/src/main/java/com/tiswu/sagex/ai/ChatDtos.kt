package com.tiswu.sagex.ai

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.3,
)

@Serializable
data class ChatCompletionResponse(
    val choices: List<Choice> = emptyList(),
) {
    @Serializable
    data class Choice(
        val message: ChatMessage? = null,
    )

    /** The assistant's text from the first choice, or null if absent. */
    fun firstContent(): String? = choices.firstOrNull()?.message?.content?.trim()?.takeIf { it.isNotEmpty() }
}
