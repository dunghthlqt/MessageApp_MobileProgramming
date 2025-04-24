package com.demo.messageapp.model

data class User (
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val isOnline: Boolean = false
)