package com.tiswu.sagex.ui.util

import android.text.format.DateUtils

/** Human-friendly relative time (e.g. "5 分钟前"), or "暂无" when absent. */
fun relativeTime(timestamp: Long?): String {
    if (timestamp == null || timestamp <= 0L) return "暂无"
    return DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS,
    ).toString()
}
