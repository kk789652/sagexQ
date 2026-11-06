package com.tiswu.sagex.notification

import com.tiswu.sagex.domain.ConversationKey
import com.tiswu.sagex.domain.NotificationParser
import com.tiswu.sagex.domain.ParseResult
import com.tiswu.sagex.domain.RawNotification
import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationType
import javax.inject.Inject

/**
 * Parses incoming WhatsApp and WhatsApp Business notifications.
 *
 * Android notification access can only capture content exposed by WhatsApp
 * notifications. It cannot retrieve chat history, suppressed notifications,
 * or messages sent by the device owner unless WhatsApp exposes them in a
 * notification. Group notifications commonly use "sender: message"; direct
 * notifications commonly expose the message body directly.
 */
class WhatsAppNotificationParser @Inject constructor() : NotificationParser {

    override fun supports(sourcePackage: String): Boolean = sourcePackage in SUPPORTED_PACKAGES

    override fun parse(raw: RawNotification): ParseResult {
        if (!supports(raw.sourcePackage)) return ParseResult.Ignored

        val title = raw.title?.trim()?.takeIf { it.isNotEmpty() }
            ?: return ParseResult.Ignored
        val rawBody = raw.text?.trim()?.takeIf { it.isNotEmpty() }
            ?: raw.lines.lastOrNull()?.trim()?.takeIf { it.isNotEmpty() }
            ?: raw.tickerText?.trim()?.takeIf { it.isNotEmpty() }
            ?: return ParseResult.Ignored

        val body = stripCountPrefix(rawBody)
        if (body.isBlank() || isSummary(body) || isSummary(title)) {
            return ParseResult.ContentUnavailable(raw.sourcePackage, title)
        }

        val match = SENDER_PREFIX.matchEntire(body)
        return if (match != null) {
            val sender = match.groupValues[1].trim()
            val content = match.groupValues[2].trim()
            if (sender.isBlank() || content.isBlank()) {
                ParseResult.Ignored
            } else {
                parsed(raw, ConversationType.GROUP, title, sender, content)
            }
        } else {
            parsed(raw, ConversationType.DIRECT, title, title, body)
        }
    }

    private fun parsed(
        raw: RawNotification,
        type: ConversationType,
        title: String,
        sender: String,
        body: String,
    ) = ParseResult.Parsed(
        CapturedMessage(
            sourcePackage = raw.sourcePackage,
            conversationKey = ConversationKey.of(raw.sourcePackage, type, title),
            conversationType = type,
            conversationTitle = title,
            sender = sender,
            body = body,
            postedAt = raw.postedAt,
        ),
    )

    private fun stripCountPrefix(value: String): String =
        COUNT_PREFIX.replaceFirst(value, "").trim()

    private fun isSummary(value: String): Boolean =
        SUMMARY_PATTERNS.any { it.containsMatchIn(value) }

    companion object {
        const val WHATSAPP_PACKAGE = "com.whatsapp"
        const val WHATSAPP_BUSINESS_PACKAGE = "com.whatsapp.w4b"
        val SUPPORTED_PACKAGES = setOf(WHATSAPP_PACKAGE, WHATSAPP_BUSINESS_PACKAGE)

        private val SENDER_PREFIX =
            Regex("^([^:：\\n]{1,48})[:：]\\s?(.*)$", RegexOption.DOT_MATCHES_ALL)
        private val COUNT_PREFIX =
            Regex("^[\\[【]\\s*\\d+\\s*(条|messages?)?\\s*[\\]】]\\s*", RegexOption.IGNORE_CASE)
        private val SUMMARY_PATTERNS = listOf(
            Regex("^\\d+\\s*(条)?\\s*(新)?消息"),
            Regex("(?i)^\\d+\\s*new messages?"),
            Regex("(?i)^\\d+\\s*messages?\\b"),
        )
    }
}
