package com.tiswu.sagex.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tiswu.sagex.domain.model.CapturedMessage
import com.tiswu.sagex.domain.model.ConversationType

@Entity(
    tableName = "messages",
    indices = [Index("conversationKey"), Index("postedAt")],
)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourcePackage: String,
    val conversationKey: String,
    val conversationType: String,
    val conversationTitle: String,
    val sender: String?,
    val body: String,
    val postedAt: Long,
)

fun CapturedMessage.toEntity(): MessageEntity = MessageEntity(
    sourcePackage = sourcePackage,
    conversationKey = conversationKey,
    conversationType = conversationType.name,
    conversationTitle = conversationTitle,
    sender = sender,
    body = body,
    postedAt = postedAt,
)

fun MessageEntity.toDomain(): CapturedMessage = CapturedMessage(
    id = id,
    sourcePackage = sourcePackage,
    conversationKey = conversationKey,
    conversationType = runCatching { ConversationType.valueOf(conversationType) }
        .getOrDefault(ConversationType.UNKNOWN),
    conversationTitle = conversationTitle,
    sender = sender,
    body = body,
    postedAt = postedAt,
)
