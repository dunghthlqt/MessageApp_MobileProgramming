package com.demo.messageapp.model.resultmodel

import com.demo.messageapp.model.User

data class GetUserListResult (
    val success: Boolean,
    val errorMessage: String?,
    val userList: List<User>?
)