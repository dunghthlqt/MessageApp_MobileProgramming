package com.demo.messageapp.model.resultmodel

data class CreateConversationResult(
    val success: Boolean,
    val errorMessage: String?,
    val conversationId: String?
)