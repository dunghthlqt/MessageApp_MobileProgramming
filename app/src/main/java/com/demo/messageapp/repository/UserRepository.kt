package com.demo.messageapp.repository

import com.demo.messageapp.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class UserRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var contactListener: ListenerRegistration? = null

    fun searchUserbyUid(userUid: String, callback: (Boolean, String?, User?) -> Unit) {
        db.collection("users")
            .whereEqualTo("uid", userUid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if(!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]

                    val uid = document.id
                    val email = document.getString("email") ?: ""
                    val displayName = document.getString("displayName") ?: ""
                    val avatarUrl = document.getString("avatarUrl") ?: ""
                    val isOnline = document.getBoolean("isOnline") ?: false

                    val user = User(
                        uid = uid,
                        email = email,
                        displayName = displayName,
                        avatarUrl = avatarUrl,
                        isOnline = isOnline
                    )

                    callback(true, null, user)
                }
            }
            .addOnFailureListener { e ->
                callback(false, e.message, null)
            }
    }
    fun getUserList(callback: (Boolean, String?, List<User>?) -> Unit) {
        db.collection("users")
            .get()
            .addOnSuccessListener { users ->
                val userList = mutableListOf<User>()
                for(user in users) {

                    val uid = user.id
                    val email = user.getString("email") ?: ""
                    val displayName = user.getString("displayName") ?: ""
                    val avatarUrl = user.getString("avatarUrl") ?: ""
                    val isOnline = user.getBoolean("isOnline") ?: false

                    val userr = User(
                        uid = uid,
                        email = email,
                        displayName = displayName,
                        avatarUrl = avatarUrl,
                        isOnline = isOnline
                    )

                    userList.add(userr)
                }
                callback(true, null, userList)
            }
            .addOnFailureListener { e ->
                callback(false, e.message, null)
            }
    }
    fun searchUserbyName(name: String, callback: (Boolean, String?, User?) -> Unit) {
        db.collection("users")
            .whereEqualTo("displayName", name)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents[0]

                val uid = document.id
                val email = document.getString("email") ?: ""
                val displayName = document.getString("displayName") ?: ""
                val avatarUrl = document.getString("avatarUrl") ?: ""
                val isOnline = document.getBoolean("isOnline") ?: false

                val user = User(
                    uid = uid,
                    email = email,
                    displayName = displayName,
                    avatarUrl = avatarUrl,
                    isOnline = isOnline
                )

                callback(true, null, user)
            }
            .addOnFailureListener { e ->
                callback(false, e.message, null)
            }
    }
    fun searchUserbyEmail(email: String, callback: (Boolean, String?, User?) -> Unit) {
        db.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents[0]

                val uid = document.id
                val email = document.getString("email") ?: ""
                val displayName = document.getString("displayName") ?: ""
                val avatarUrl = document.getString("avatarUrl") ?: ""
                val isOnline = document.getBoolean("isOnline") ?: false

                val user = User(
                    uid = uid,
                    email = email,
                    displayName = displayName,
                    avatarUrl = avatarUrl,
                    isOnline = isOnline
                )

                callback(true, null, user)
            }
            .addOnFailureListener { e ->
                callback(false, e.message, null)
            }
    }
    fun updateDisplayName(userUid: String, newName: String, callback: (Boolean, String?) -> Unit) {
        db.collection("users")
            .document(userUid)
            .update("displayName", newName)
            .addOnSuccessListener { _ ->
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }
    fun getContactsUIDList(userUid: String, callback: (Boolean, String?, List<String>?) -> Unit) {
        db.collection("users")
            .document(userUid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val contactsUIDList = documentSnapshot.get("contacts") as? List<String> ?: listOf()
                callback(true, null, contactsUIDList)
            }
            .addOnFailureListener { e ->
                callback(false, e.message, null)
            }
    }
    fun addNewContact(userUid: String, contactUid: String, callback: (Boolean, String?) -> Unit) {
        db.collection("users")
            .document(userUid)
            .update("contacts", FieldValue.arrayUnion(contactUid))
            .addOnSuccessListener { _ ->
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }
    fun addContactListener(userUid: String, callback: (Boolean, String?, List<String>?) -> Unit) {
        contactListener?.remove()

        contactListener = db.collection("users")
            .document(userUid)
            .addSnapshotListener { documentSnapshot, e ->
                if (e != null || documentSnapshot == null) {
                    callback(false, "Snapshot is null", null)
                    return@addSnapshotListener
                }

                val contactsUIDList = documentSnapshot.get("contacts") as? List<String> ?: listOf()
                callback(true, null, contactsUIDList)
            }
    }
    fun removeContactListener() {
        contactListener?.remove()
    }
}