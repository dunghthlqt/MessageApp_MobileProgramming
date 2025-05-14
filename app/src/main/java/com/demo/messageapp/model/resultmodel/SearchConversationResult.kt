package com.demo.messageapp.model.resultmodel

import com.demo.messageapp.model.Conversation

data class SearchConversationResult (
    val success: Boolean,
    val errorMessage: String?,
    val conversation: Conversation?
)