package com.demo.messageapp.model

data class Conversation (
    val id: String = "",
    val conversationName: String = "",
    val participantIds: List<String> = listOf(),
    val createdAt: Long = 0,
    val lastMessage: String = "",
    val deleted: Boolean = false,
    val createBy: String = ""
)
