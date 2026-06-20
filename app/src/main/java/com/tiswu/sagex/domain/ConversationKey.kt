package com.tiswu.sagex.domain

import com.tiswu.sagex.domain.model.ConversationType
import java.security.MessageDigest

/**
 * Derives a stable, opaque conversation key.
 *
 * Notifications do not carry a stable chat id, so we derive one from
 * (sourcePackage, conversationType, conversationTitle). Including the type
 * keeps a group and a direct chat that share a display name distinct.
 */
object ConversationKey {

    fun of(sourcePackage: String, type: ConversationType, title: String): String {
        val composite = "$sourcePackage|${type.name}|$title"
        val digest = MessageDigest.getInstance("SHA-256").digest(composite.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { "%02x".format(it) }
    }
}
