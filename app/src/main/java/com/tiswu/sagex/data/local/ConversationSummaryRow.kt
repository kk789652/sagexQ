package com.tiswu.sagex.data.local

import com.tiswu.sagex.domain.model.ConversationSummary
import com.tiswu.sagex.domain.model.ConversationType

/** Projection row for the distinct-conversation aggregate query. */
data class ConversationSummaryRow(
    val conversationKey: String,
    val conversationTitle: String,
    val conversationType: String,
    val messageCount: Int,
    val lastPostedAt: Long,
)

fun ConversationSummaryRow.toDomain(): ConversationSummary = ConversationSummary(
    conversationKey = conversationKey,
    conversationTitle = conversationTitle,
    conversationType = runCatching { ConversationType.valueOf(conversationType) }
        .getOrDefault(ConversationType.UNKNOWN),
    messageCount = messageCount,
    lastPostedAt = lastPostedAt,
)
