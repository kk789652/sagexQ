package com.tiswu.sagex.domain

import com.tiswu.sagex.domain.model.CapturedMessage
import java.time.Instant
import java.time.ZoneId

/** A selectable time window, used by summarize / delete / export. */
sealed interface TimeRange {
    val label: String

    data object All : TimeRange {
        override val label = "全部"
    }

    data object Today : TimeRange {
        override val label = "今天"
    }

    data object Last7Days : TimeRange {
        override val label = "近 7 天"
    }

    data object Last30Days : TimeRange {
        override val label = "近 30 天"
    }

    /** Inclusive [startMillis], exclusive [endMillis]. */
    data class Custom(val startMillis: Long, val endMillis: Long) : TimeRange {
        override val label = "自定义"
    }

    companion object {
        val presets: List<TimeRange> = listOf(All, Today, Last7Days, Last30Days)
        const val DAY_MILLIS: Long = 24L * 60 * 60 * 1000
    }
}

/** Resolve the range to concrete bounds `[startInclusive, endExclusive)` relative to [now]. */
fun TimeRange.bounds(now: Long, zone: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
    val endInclusiveOfNow = now + 1
    return when (this) {
        TimeRange.All -> 0L to Long.MAX_VALUE
        TimeRange.Today -> startOfDay(now, zone) to endInclusiveOfNow
        TimeRange.Last7Days -> (now - 7 * TimeRange.DAY_MILLIS) to endInclusiveOfNow
        TimeRange.Last30Days -> (now - 30 * TimeRange.DAY_MILLIS) to endInclusiveOfNow
        is TimeRange.Custom -> startMillis to endMillis
    }
}

/** Pure: keep only messages whose timestamp falls within [range] relative to [now]. */
fun filterByRange(
    messages: List<CapturedMessage>,
    range: TimeRange,
    now: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): List<CapturedMessage> {
    val (start, end) = range.bounds(now, zone)
    return messages.filter { it.postedAt in start until end }
}

private fun startOfDay(now: Long, zone: ZoneId): Long =
    Instant.ofEpochMilli(now).atZone(zone).toLocalDate().atStartOfDay(zone).toInstant().toEpochMilli()
