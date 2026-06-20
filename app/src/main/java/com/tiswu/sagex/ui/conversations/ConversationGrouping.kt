package com.tiswu.sagex.ui.conversations

import com.tiswu.sagex.domain.model.ConversationSummary
import com.tiswu.sagex.domain.model.ConversationType

/** A list section: all conversations of one [type], preserving input (recency) order. */
data class ConversationSection(
    val type: ConversationType,
    val items: List<ConversationSummary>,
)

/** Pure grouping of a flat conversation list into ordered, non-empty type sections. */
object ConversationGrouping {

    private val SECTION_ORDER = listOf(
        ConversationType.GROUP,
        ConversationType.DIRECT,
        ConversationType.UNKNOWN,
    )

    fun intoSections(conversations: List<ConversationSummary>): List<ConversationSection> =
        SECTION_ORDER.mapNotNull { type ->
            val items = conversations.filter { it.conversationType == type }
            if (items.isEmpty()) null else ConversationSection(type, items)
        }
}
