package com.tiswu.sagex.domain.model

/** Aggregate view of one source app on the home screen. */
data class AppSummary(
    val sourcePackage: String,
    val displayName: String,
    val conversationCount: Int,
    val messageCount: Int,
    val lastPostedAt: Long,
)
