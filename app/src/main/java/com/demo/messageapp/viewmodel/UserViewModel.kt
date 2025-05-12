package com.demo.messageapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.messageapp.model.resultmodel.GetUserListResult
import com.demo.messageapp.model.resultmodel.SearchUserResult
import com.demo.messageapp.model.resultmodel.GetContactsUIDListResult
import com.demo.messageapp.repository.UserRepository

class UserViewModel : ViewModel() {
    private val repository = UserRepository()

    val searchUserbyUidResult = MutableLiveData<SearchUserResult>()
    val getUserListResult = MutableLiveData<GetUserListResult>()
    val searchUserbyNameResult = MutableLiveData<SearchUserResult>()
    val searchUserbyEmailResult = MutableLiveData<SearchUserResult>()
    val updateDisplayNameResult = MutableLiveData<Pair<Boolean, String?>>()
    val getContactsUIDListResult = MutableLiveData<GetContactsUIDListResult>()
    val addNewContactResult = MutableLiveData<Pair<Boolean, String?>>()

    fun searchUserbyUid(userUid: String) {
        repository.searchUserbyUid(userUid) { success, message, user ->
            searchUserbyUidResult.value = SearchUserResult(success, message, user)
        }
    }
    fun searchUserbyName(name: String) {
        repository.searchUserbyName(name) { success, message, user ->
            searchUserbyNameResult.value = SearchUserResult(success, message, user)
        }
    }
    fun searchUserbyEmail(email: String) {
        repository.searchUserbyEmail(email) { success, message, user ->
            searchUserbyEmailResult.value = SearchUserResult(success, message, user)
        }
    }
    fun getUserList(targetUids: List<String>) {
        repository.getUserList { success, message, userList ->
            if (success && userList != null) {
                val filteredUsers = userList.filter { it.uid in targetUids }
                getUserListResult.value = GetUserListResult(true, null, filteredUsers)
            } else {
                getUserListResult.value = GetUserListResult(false, message, null)
            }
        }
    }
    fun updateDisplayName(userUid: String, newName: String) {
        repository.updateDisplayName(userUid, newName) { success, message ->
            updateDisplayNameResult.value = Pair(success, message)
        }
    }
    fun getContactsUIDList(userUid: String) {
        repository.getContactsUIDList(userUid) { success, message, contactsUIDList ->
            getContactsUIDListResult.value = GetContactsUIDListResult(success, message, contactsUIDList)
        }
    }
    fun addNewContact(userUid: String, contactUid: String) {
        repository.addNewContact(userUid, contactUid) { success, message ->
            addNewContactResult.value = Pair(success, message)
        }
    }
    fun addContactListener(userUid: String) {
        repository.addContactListener(userUid) { success, message, contactsUIDList ->
            getContactsUIDListResult.value = GetContactsUIDListResult(success, message, contactsUIDList)
        }
    }
    fun removeContactListener() {
        repository.removeContactListener()
    }
}