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
 * - WeChat prefixes the text with an unread count ("[N条]") when a conversation has
 *   more than one unread message; the prefix is stripped and the remainder parsed.
 * - Pure count summaries ("2条新消息" / "2 new messages") carry no content -> [ParseResult.ContentUnavailable].
 * - Missing/empty title or text -> [ParseResult.Ignored].
 */
class WeChatNotificationParser @Inject constructor() : NotificationParser {

    override fun supports(sourcePackage: String): Boolean = sourcePackage == WECHAT_PACKAGE

    override fun parse(raw: RawNotification): ParseResult {
        if (raw.sourcePackage != WECHAT_PACKAGE) return ParseResult.Ignored

        val title = raw.title?.trim().orEmptyOrNull()
        val rawBody = resolveBody(raw)

        if (title == null || rawBody == null) return ParseResult.Ignored

        // Strip a leading "[N条]" unread-count prefix before classifying.
        val body = stripCountPrefix(rawBody)

        // Content-unavailable only when nothing real remains (bracket-only or a pure summary).
        if (body.isEmpty() || isPureSummary(body) || isPureSummary(title)) {
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

    /** Remove a leading "[N条]" / "【N条】" unread-count prefix, returning the trimmed remainder. */
    private fun stripCountPrefix(value: String): String =
        COUNT_PREFIX.replaceFirst(value, "").trim()

    private fun isPureSummary(value: String): Boolean = PURE_SUMMARY_PATTERNS.any { it.containsMatchIn(value) }

    private fun String?.orEmptyOrNull(): String? = this?.takeIf { it.isNotEmpty() }

    companion object {
        const val WECHAT_PACKAGE = "com.tencent.mm"

        /** "Sender: content" — sender is a short, single-line, colon-free prefix (ASCII or fullwidth colon). */
        private val SENDER_PREFIX = Regex("^([^:：\\n]{1,32})[:：]\\s?(.*)$", RegexOption.DOT_MATCHES_ALL)

        /** Leading unread-count prefix: "[N条]" or fullwidth "【N条】", optional surrounding spaces. */
        private val COUNT_PREFIX = Regex("^[\\[【]\\s*\\d+\\s*条\\s*[\\]】]\\s*")

        /**
         * Content-less summary notifications (after any bracket prefix has been stripped).
         * Anchored at start so they never match a real message that merely contains these words.
         */
        private val PURE_SUMMARY_PATTERNS = listOf(
            Regex("^\\d+\\s*条\\s*(新)?消息"),            // "2条新消息", "5条消息来自3个聊天"
            Regex("^\\d+\\s*位联系人"),                   // "N位联系人发来..."
            Regex("^来自\\d+\\s*个(聊天|会话)"),           // "来自2个聊天" (multi-chat summary after strip)
            Regex("(?i)^\\d+\\s*new messages?"),         // "2 new messages"
            Regex("(?i)^\\d+\\s*messages?\\b"),          // "3 messages"
        )
    }
}
