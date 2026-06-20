package com.tiswu.sagex.ai

import com.tiswu.sagex.domain.model.CapturedMessage
import java.time.Instant
import java.time.ZoneId

/** Time window for a summary request. */
enum class MessageRange(val label: String) {
    ALL("全部"),
    TODAY("今天"),
    LAST_7_DAYS("近 7 天"),
}

/** Pure: keep only messages whose timestamp falls within [range] relative to [now]. */
fun filterByRange(
    messages: List<CapturedMessage>,
    range: MessageRange,
    now: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): List<CapturedMessage> = when (range) {
    MessageRange.ALL -> messages
    MessageRange.TODAY -> {
        val startOfDay = Instant.ofEpochMilli(now)
            .atZone(zone)
            .toLocalDate()
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        messages.filter { it.postedAt >= startOfDay }
    }
    MessageRange.LAST_7_DAYS -> {
        val cutoff = now - 7L * 24 * 60 * 60 * 1000
        messages.filter { it.postedAt >= cutoff }
    }
}
