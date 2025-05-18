package com.demo.messageapp.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.demo.messageapp.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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

                    val lastSeen: Long = when (val lastSeenValue = document.get("lastSeen")) {
                        is Long -> lastSeenValue
                        is Number -> lastSeenValue.toLong()
                        is Timestamp -> lastSeenValue.toDate().time
                        is String -> try {
                            lastSeenValue.toLong()
                        } catch (e: NumberFormatException) {
                            0L
                        }
                        else -> 0L
                    }

                    val user = User(
                        uid = uid,
                        email = email,
                        displayName = displayName,
                        avatarUrl = avatarUrl,
                        isOnline = isOnline,
                        lastSeen = lastSeen
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

                    val lastSeen: Long = when (val lastSeenValue = user.get("lastSeen")) {
                        is Long -> lastSeenValue
                        is Number -> lastSeenValue.toLong()
                        is Timestamp -> lastSeenValue.toDate().time
                        is String -> try {
                            lastSeenValue.toLong()
                        } catch (e: NumberFormatException) {
                            0L
                        }
                        else -> 0L
                    }

                    val userr = User(
                        uid = uid,
                        email = email,
                        displayName = displayName,
                        avatarUrl = avatarUrl,
                        isOnline = isOnline,
                        lastSeen = lastSeen
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
                val lastSeen = document.getLong("lastSeen") ?: 0

                val user = User(
                    uid = uid,
                    email = email,
                    displayName = displayName,
                    avatarUrl = avatarUrl,
                    isOnline = isOnline,
                    lastSeen = lastSeen
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
                if(!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]

                    val uid = document.id
                    val email = document.getString("email") ?: ""
                    val displayName = document.getString("displayName") ?: ""
                    val avatarUrl = document.getString("avatarUrl") ?: ""
                    val isOnline = document.getBoolean("isOnline") ?: false
                    val lastSeen = document.getLong("lastSeen") ?: 0

                    val user = User(
                        uid = uid,
                        email = email,
                        displayName = displayName,
                        avatarUrl = avatarUrl,
                        isOnline = isOnline,
                        lastSeen = lastSeen
                    )

                    callback(true, null, user)
                } else {
                    callback(false, null, null)
                }
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
    fun updateUserProfile(displayName: String, imageUri: Uri?, context: Context, callback: (Boolean, String?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            callback(false, "Người dùng chưa đăng nhập")
            return
        }

        if (imageUri != null) {
            val filePath = getRealPathFromUri(context, imageUri)
            if (filePath != null) {
                MediaManager.get().upload(filePath)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {

                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {

                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            var imageUrl = resultData["url"] as String
                            if (imageUrl.startsWith("http://")) {
                                imageUrl = imageUrl.replace("http://", "https://")
                            }
                            db.collection("users")
                                .document(user.uid)
                                .update("displayName", displayName)
                                .addOnSuccessListener { _ ->
                                    db.collection("users")
                                        .document(user.uid)
                                        .update("avatarUrl", imageUrl)
                                        .addOnSuccessListener { _ ->
                                            callback(true, null)
                                        }
                                        .addOnFailureListener { e ->
                                            callback(false, e.message)
                                        }
                                }
                                .addOnFailureListener { e ->
                                    callback(false, e.message)
                                }
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            callback(false, "Tải ảnh thất bại: ${error.description}")
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            // Không xử lý reschedule
                        }
                    })
                    .dispatch()
            } else {
                callback(false, "Không thể lấy đường dẫn tệp ảnh")
            }
        } else {
            db.collection("users")
                .document(user.uid)
                .update("displayName", displayName)
                .addOnSuccessListener { _ ->
                    callback(true, null)
                }
                .addOnFailureListener { e ->
                    callback(false, e.message)
                }
        }
    }

    private fun getRealPathFromUri(context: Context, uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndexOrThrow("_data")
            it.getString(columnIndex)
        }
    }
}