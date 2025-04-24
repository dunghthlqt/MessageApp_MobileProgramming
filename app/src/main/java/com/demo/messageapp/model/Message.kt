package com.demo.messageapp.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val type: String = "",
    val deleted: Boolean = false
)
