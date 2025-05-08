package com.demo.messageapp.model

data class User (
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val isOnline: Boolean = false,
    val joinedConversation: List<String> = listOf(),

    //duc them cai nay
    val messages: List<Message> = listOf()
)