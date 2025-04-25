package com.demo.messageapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.messageapp.model.resultmodel.GetUserListResult
import com.demo.messageapp.model.resultmodel.SearchUserResult
import com.demo.messageapp.repository.UserRepository

class UserViewModel : ViewModel() {
    private val repository = UserRepository()

    val searchUserbyUidResult = MutableLiveData<SearchUserResult>()
    val getUserListResult = MutableLiveData<GetUserListResult>()
    val searchUserbyNameResult = MutableLiveData<SearchUserResult>()
    val searchUserbyEmailResult = MutableLiveData<SearchUserResult>()
    val updateDisplayNameResult = MutableLiveData<Pair<Boolean, String?>>()

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
    fun getUserList() {
        repository.getUserList() { success, message, userList ->
            getUserListResult.value = GetUserListResult(success, message, userList)
        }
    }
    fun updateDisplayName(userUid: String, newName: String) {
        repository.updateDisplayName(userUid, newName) { success, message ->
            updateDisplayNameResult.value = Pair(success, message)
        }
    }
}