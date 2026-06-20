package com.tiswu.sagex.domain

import com.tiswu.sagex.domain.model.CapturedMessage

/**
 * Source-agnostic view of a posted notification, decoupled from Android's
 * `StatusBarNotification` so parsers can be unit-tested as pure functions.
 */
data class RawNotification(
    val sourcePackage: String,
    val title: String?,
    val text: String?,
    val lines: List<String> = emptyList(),
    val tickerText: String? = null,
    val postedAt: Long,
)

/** Outcome of attempting to parse a [RawNotification]. */
sealed interface ParseResult {

    /** Successfully parsed into a structured message. */
    data class Parsed(val message: CapturedMessage) : ParseResult

    /**
     * Recognized as a relevant notification, but it carried no usable content
     * (e.g. an aggregated "2 new messages" summary).
     */
    data class ContentUnavailable(
        val sourcePackage: String,
        val conversationTitle: String?,
    ) : ParseResult

    /** Not relevant; should be dropped. */
    data object Ignored : ParseResult
}

/** Parses notifications from a specific source app into structured messages. */
interface NotificationParser {

    /** Whether this parser handles notifications from [sourcePackage]. */
    fun supports(sourcePackage: String): Boolean

    fun parse(raw: RawNotification): ParseResult
}
