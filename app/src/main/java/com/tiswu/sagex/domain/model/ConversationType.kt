package com.tiswu.sagex.domain.model

/** Whether a captured conversation is a group chat, a one-to-one chat, or could not be determined. */
enum class ConversationType {
    GROUP,
    DIRECT,
    UNKNOWN,
}
