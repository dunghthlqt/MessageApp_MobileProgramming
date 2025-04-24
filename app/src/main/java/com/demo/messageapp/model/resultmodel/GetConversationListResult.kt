package com.demo.messageapp.model.resultmodel

import com.demo.messageapp.model.Conversation

data class GetConversationListResult (
    val success: Boolean,
    val errorMessage: String?,
    val conversationList: List<Conversation>?
)