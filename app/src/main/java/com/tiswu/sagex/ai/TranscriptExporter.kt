package com.tiswu.sagex.ai

import com.tiswu.sagex.domain.model.CapturedMessage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Renders a conversation's (already range-filtered) messages into shareable plain text. Pure. */
object TranscriptExporter {

    // Safety cap against the Android Binder transaction size limit (TransactionTooLargeException
    // on the share Intent). High enough to cover a full busy day of one chat; truncation, when it
    // happens, is surfaced to the user instead of silently dropping messages.
    const val DEFAULT_MAX_MESSAGES = 2000

    private val TIME_FORMAT = DateTimeFormatter.ofPattern("MM-dd HH:mm")

    /**
     * @param instructionHeader optional prompt prefixed before the transcript (blank = omitted).
     * @param max cap on included messages; when exceeded, only the most recent [max] are used and a
     *   note is added so the reader knows the transcript is partial.
     */
    fun export(
        conversationTitle: String,
        messages: List<CapturedMessage>,
        instructionHeader: String,
        max: Int = DEFAULT_MAX_MESSAGES,
        zone: ZoneId = ZoneId.systemDefault(),
    ): String {
        val truncated = messages.size > max
        val recent = if (truncated) messages.takeLast(max) else messages
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
            if (truncated) {
                append("(注:该范围共 ${messages.size} 条消息,因体积限制仅包含最近 $max 条)")
                append("\n\n")
            }
            append(transcript)
        }
    }
}
