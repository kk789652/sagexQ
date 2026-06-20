package com.tiswu.sagex.ai

import com.tiswu.sagex.domain.model.CapturedMessage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Builds the chat messages for a summary request from captured messages. Pure. */
object SummaryPromptBuilder {

    const val DEFAULT_MAX_MESSAGES = 200

    private val TIME_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm")

    /**
     * @param systemPrompt system instruction to send with the summary request.
     * @param max cap on included messages; when exceeded, only the most recent [max] are used.
     * @return system + user chat messages, or an empty list if there is nothing to summarize.
     */
    fun build(
        conversationTitle: String,
        messages: List<CapturedMessage>,
        systemPrompt: String,
        max: Int = DEFAULT_MAX_MESSAGES,
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<ChatMessage> {
        if (messages.isEmpty()) return emptyList()
        val recent = if (messages.size > max) messages.takeLast(max) else messages
        val transcript = recent.joinToString(separator = "\n") { line ->
            val time = TIME_FORMAT.format(Instant.ofEpochMilli(line.postedAt).atZone(zone))
            val sender = line.sender?.takeIf { it.isNotBlank() } ?: "?"
            "[$time] $sender: ${line.body}"
        }
        val user = "会话:$conversationTitle\n\n以下是聊天记录:\n$transcript"
        return listOf(
            ChatMessage(role = "system", content = systemPrompt),
            ChatMessage(role = "user", content = user),
        )
    }
}
