package com.demo.messageapp.model.resultmodel

data class GetContactsUIDListResult (
    val success: Boolean,
    val errorMessage: String?,
    val contactsUIDList: List<String>?
)