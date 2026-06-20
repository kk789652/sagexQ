package com.tiswu.sagex.notification

import com.tiswu.sagex.domain.ConversationKey
import com.tiswu.sagex.domain.NotificationParser
import com.tiswu.sagex.domain.ParseResult
import com.tiswu.sagex.domain.RawNotification
import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationType
import javax.inject.Inject

/**
 * Parses WeChat (`com.tencent.mm`) notifications.
 *
 * Heuristics (all WeChat format brittleness lives here):
 * - Group message: title = group name, text = "Sender: content".
 * - Direct message: title = contact name, text = content (no "Sender:" prefix).
 * - Aggregated summaries ("2 new messages" / "2条新消息") carry no content -> [ParseResult.ContentUnavailable].
 * - Missing/empty title or text -> [ParseResult.Ignored].
 */
class WeChatNotificationParser @Inject constructor() : NotificationParser {

    override fun supports(sourcePackage: String): Boolean = sourcePackage == WECHAT_PACKAGE

    override fun parse(raw: RawNotification): ParseResult {
        if (raw.sourcePackage != WECHAT_PACKAGE) return ParseResult.Ignored

        val title = raw.title?.trim().orEmptyOrNull()
        val body = resolveBody(raw)

        if (title == null || body == null) return ParseResult.Ignored

        if (isAggregated(body) || isAggregated(title)) {
            return ParseResult.ContentUnavailable(raw.sourcePackage, title)
        }

        val senderSplit = SENDER_PREFIX.matchEntire(body)
        return if (senderSplit != null) {
            val sender = senderSplit.groupValues[1].trim()
            val content = senderSplit.groupValues[2].trim()
            if (content.isEmpty()) {
                ParseResult.Ignored
            } else {
                buildMessage(raw, ConversationType.GROUP, title, sender, content)
            }
        } else {
            buildMessage(raw, ConversationType.DIRECT, title, sender = title, content = body)
        }
    }

    private fun buildMessage(
        raw: RawNotification,
        type: ConversationType,
        title: String,
        sender: String,
        content: String,
    ): ParseResult.Parsed {
        return ParseResult.Parsed(
            CapturedMessage(
                sourcePackage = raw.sourcePackage,
                conversationKey = ConversationKey.of(raw.sourcePackage, type, title),
                conversationType = type,
                conversationTitle = title,
                sender = sender,
                body = content,
                postedAt = raw.postedAt,
            ),
        )
    }

    /** Prefer EXTRA_TEXT, fall back to the last EXTRA_LINES entry, then tickerText. */
    private fun resolveBody(raw: RawNotification): String? =
        raw.text?.trim().orEmptyOrNull()
            ?: raw.lines.lastOrNull()?.trim().orEmptyOrNull()
            ?: raw.tickerText?.trim().orEmptyOrNull()

    private fun isAggregated(value: String): Boolean = AGGREGATED_PATTERNS.any { it.containsMatchIn(value) }

    private fun String?.orEmptyOrNull(): String? = this?.takeIf { it.isNotEmpty() }

    companion object {
        const val WECHAT_PACKAGE = "com.tencent.mm"

        /** "Sender: content" — sender is a short, single-line, colon-free prefix (ASCII or fullwidth colon). */
        private val SENDER_PREFIX = Regex("^([^:：\\n]{1,32})[:：]\\s?(.*)$", RegexOption.DOT_MATCHES_ALL)

        private val AGGREGATED_PATTERNS = listOf(
            Regex("^\\[?\\s*\\d+\\s*条"),                 // "2条新消息", "[3条]", "5条消息来自..."
            Regex("(?i)^\\[?\\s*\\d+\\s*new messages?"),  // "2 new messages"
            Regex("(?i)^\\[?\\s*\\d+\\s*messages?\\b"),   // "3 messages"
        )
    }
}
