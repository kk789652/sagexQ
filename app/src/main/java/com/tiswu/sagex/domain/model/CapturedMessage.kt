package com.tiswu.sagex.domain.model

/**
 * A single message captured from a notification, normalized into a source-agnostic form.
 *
 * @param sourcePackage the app the notification came from (e.g. `com.tencent.mm`)
 * @param conversationKey stable key grouping messages from the same conversation; see [ConversationKey]
 * @param conversationType group vs. direct vs. unknown
 * @param conversationTitle display name of the conversation (group name or contact name)
 * @param sender display name of the sender, when distinguishable from the conversation
 * @param body the message text
 * @param postedAt notification post time, epoch millis
 */
data class CapturedMessage(
    val id: Long = 0,
    val sourcePackage: String,
    val conversationKey: String,
    val conversationType: ConversationType,
    val conversationTitle: String,
    val sender: String?,
    val body: String,
    val postedAt: Long,
)
