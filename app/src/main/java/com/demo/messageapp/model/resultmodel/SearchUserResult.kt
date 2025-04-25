package com.demo.messageapp.model.resultmodel

import com.demo.messageapp.model.User

data class SearchUserResult (
    val success: Boolean,
    val errorMessage: String?,
    val user: User?
)