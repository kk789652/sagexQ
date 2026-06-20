package com.tiswu.sagex.ai

import com.tiswu.sagex.domain.model.CapturedMessage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Builds the chat messages for a summary request from captured messages. Pure. */
object SummaryPromptBuilder {

    const val DEFAULT_MAX_MESSAGES = 200

    private const val SYSTEM_PROMPT =
        "你是一个聊天记录总结助手。请用简洁的中文总结以下聊天内容,提炼要点、待办事项和需要关注的重要信息。" +
            "如果有人提出问题或请求,请单独列出。"

    private val TIME_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm")

    /**
     * @param max cap on included messages; when exceeded, only the most recent [max] are used.
     * @return system + user chat messages, or an empty list if there is nothing to summarize.
     */
    fun build(
        conversationTitle: String,
        messages: List<CapturedMessage>,
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
            ChatMessage(role = "system", content = SYSTEM_PROMPT),
            ChatMessage(role = "user", content = user),
        )
    }
}
