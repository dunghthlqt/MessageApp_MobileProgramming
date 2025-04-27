package com.demo.messageapp.model.resultmodel

import com.demo.messageapp.model.Message

data class SearchMessageResult (
    val success: Boolean,
    val errorMessage: String?,
    val messageList: List<Message>?
)