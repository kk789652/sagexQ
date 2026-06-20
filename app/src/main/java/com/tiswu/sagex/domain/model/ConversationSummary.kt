package com.tiswu.sagex.domain.model

/** Aggregate view of one conversation, used for list/status display. */
data class ConversationSummary(
    val conversationKey: String,
    val conversationTitle: String,
    val conversationType: ConversationType,
    val messageCount: Int,
    val lastPostedAt: Long,
    val lastMessage: String,
    val lastSender: String?,
)
