package com.demo.messageapp.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    var isSentByMe: Boolean = false,
    val content: String = "",
    val timestamp: Long = 0,
    val type: String = "",
    val deleted: Boolean = false,
    var reactions: List<Reaction> = listOf(),
    val replyInfo: ReplyInfo? = null
)

data class Reaction(
    val emoji: String,
    val userId: String
)

data class ReplyInfo(
    val originalMessageId: String,
    val originalSenderId: String,
    val replyContent: String
)