package com.demo.messageapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.messageapp.model.User
import com.demo.messageapp.repository.AuthRepository

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    val registerResult = MutableLiveData<Pair<Boolean, String?>>()
    val loginResult = MutableLiveData<Pair<Boolean, String?>>()
    val currentUser = MutableLiveData<User?>()
    val sendEmailVerificationResult = MutableLiveData<Pair<Boolean, String?>>()
    val updatePasswordResult = MutableLiveData<Pair<Boolean, String?>>()
    val createUserProfileResult = MutableLiveData<Pair<Boolean, String?>>()

    fun register(email: String, password: String) {
        repository.register(email, password) { success, message -> registerResult.value = Pair(success, message) }
    }
    fun login(email: String, password: String) {
        repository.login(email, password) { success, message -> loginResult.value = Pair(success, message) }
    }
    fun logout() {
        repository.logout() { _, _ -> currentUser.value = null}
    }
    fun getCurrentUser() {
        val firebaseUser = repository.getCurrentUser()
        if (firebaseUser != null) {
            currentUser.value = User(uid = firebaseUser.uid, email = firebaseUser.email ?: "")
        } else {
            currentUser.value = null
        }
    }
    fun isUserVerified() : Boolean {
        val firebaseUser = repository.getCurrentUser()
        if (firebaseUser!!.isEmailVerified) {
            return true
        } else {
            return false
        }
    }
    fun sendEmailVerification() {
        repository.sendEmailVerification { success, message -> sendEmailVerificationResult.value = Pair(success, message) }
    }
    fun updatePassword(newPassword: String) {
        repository.updatePassword(newPassword){ success, message -> updatePasswordResult.value = Pair(success, message) }
    }
    fun createUserProfile() {
        repository.createUserProfile{ success, message -> createUserProfileResult.value = Pair(success, message) }
    }
}