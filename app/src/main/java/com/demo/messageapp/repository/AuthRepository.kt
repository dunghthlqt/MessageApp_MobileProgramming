package com.demo.messageapp.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.auth.ktx.auth
//import com.google.firebase.ktx.Firebase

class AuthRepository {
//  private val auth: FirebaseAuth = Firebase.auth
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    fun register(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun login(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun sendEmailVerification(callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser

        user!!.sendEmailVerification()
              .addOnCompleteListener {task ->
                  if(task.isSuccessful) {
                      callback(true, null)
                  } else {
                      callback(false, task.exception?.message)
                  }
              }
    }

    fun updatePassword(newPassword: String, callback: (Boolean, String?) -> Unit) {
        val user = auth.currentUser

        user!!.updatePassword(newPassword)
              .addOnCompleteListener { task ->
                  if (task.isSuccessful) {
                      callback(true, null)
                  } else {
                      callback(false, task.exception?.message)
                  }
              }
    }
}