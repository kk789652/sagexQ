package com.tiswu.sagex.data.local

import com.tiswu.sagex.domain.appDisplayName
import com.tiswu.sagex.domain.model.AppSummary

/** Projection row for the per-source-app aggregate query. */
data class AppSummaryRow(
    val sourcePackage: String,
    val conversationCount: Int,
    val messageCount: Int,
    val lastPostedAt: Long,
)

fun AppSummaryRow.toDomain(): AppSummary = AppSummary(
    sourcePackage = sourcePackage,
    displayName = appDisplayName(sourcePackage),
    conversationCount = conversationCount,
    messageCount = messageCount,
    lastPostedAt = lastPostedAt,
)
