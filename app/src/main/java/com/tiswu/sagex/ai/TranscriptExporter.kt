package com.tiswu.sagex.ai

import com.tiswu.sagex.domain.model.CapturedMessage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Renders a conversation's (already range-filtered) messages into shareable plain text. Pure. */
object TranscriptExporter {

    const val DEFAULT_MAX_MESSAGES = 500

    private val TIME_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm")

    /**
     * @param instructionHeader optional prompt prefixed before the transcript (blank = omitted).
     * @param max cap on included messages; when exceeded, only the most recent [max] are used.
     */
    fun export(
        conversationTitle: String,
        messages: List<CapturedMessage>,
        instructionHeader: String,
        max: Int = DEFAULT_MAX_MESSAGES,
        zone: ZoneId = ZoneId.systemDefault(),
    ): String {
        val recent = if (messages.size > max) messages.takeLast(max) else messages
        val transcript = recent.joinToString(separator = "\n") { line ->
            val time = TIME_FORMAT.format(Instant.ofEpochMilli(line.postedAt).atZone(zone))
            val sender = line.sender?.takeIf { it.isNotBlank() } ?: "?"
            "[$time] $sender: ${line.body}"
        }
        return buildString {
            if (instructionHeader.isNotBlank()) {
                append(instructionHeader.trim())
                append("\n\n")
            }
            append("会话:")
            append(conversationTitle)
            append("\n\n")
            append(transcript)
        }
    }
}
