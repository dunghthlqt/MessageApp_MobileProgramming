package com.demo.messageapp.repository

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.demo.messageapp.model.Conversation
import com.demo.messageapp.viewmodel.ConversationViewModel
import com.demo.messageapp.viewmodel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore

class ConversationRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun createConversation(participantIds: List<String>, conversationName: String, currentUserId: String, callback: (Boolean, String?, String?) -> Unit) {
        val conversationRef = db.collection("conversations").document()
        val conversationId = conversationRef.id

        if (conversationName.isNotEmpty()) {
            // Nếu có tên cuộc trò chuyện, tạo và lưu cuộc trò chuyện ngay lập tức
            val conversation = Conversation(
                id = conversationId,
                createdAt = System.currentTimeMillis(),
                participantIds = participantIds,
                conversationName = conversationName,
                createBy = currentUserId
            )

            conversationRef.set(conversation)
                .addOnSuccessListener { _ ->
                    callback(true, null, conversationId)
                }
                .addOnFailureListener { e ->
                    callback(false, e.message, null)
                }
        } else {
            // Nếu không có tên cuộc trò chuyện, tìm người dùng khác để lấy tên
            val otherUserId = participantIds.find { it != currentUserId }

            if (otherUserId != null) {
                val userRepository = UserRepository()
                userRepository.searchUserbyUid(otherUserId) { success, errorMessage, user ->
                    if (success && user != null) {
                        // Tạo cuộc trò chuyện với tên là tên người dùng khác
                        val conversation = Conversation(
                            id = conversationId,
                            createdAt = System.currentTimeMillis(),
                            participantIds = participantIds,
                            conversationName = user.displayName,
                            createBy = currentUserId
                        )

                        conversationRef.set(conversation)
                            .addOnSuccessListener { _ ->
                                callback(true, null, conversationId)
                            }
                            .addOnFailureListener { e ->
                                callback(false, e.message, null)
                            }
                    }
                }
            }
        }
    }
    fun deleteConversation(conversationId: String, callback: (Boolean, String?) -> Unit) {
        db.collection("conversations")
            .document(conversationId)
            .update("deleted", true)
            .addOnSuccessListener { _ ->
                callback(true, null)
            }
            .addOnFailureListener { task ->
                callback(false, task.message)
            }
    }

    fun getConversationList(userUid: String, callback: (Boolean, String?, List<Conversation>?) -> Unit) {
        val userCollection = db.collection("users")

        Log.d("ConversationRepository", "userUid = $userUid")

        userCollection.document(userUid)
            .get()
            .addOnSuccessListener { document ->
                val joinedConversation = document.get("joinedConversations") as? List<String>

                if (joinedConversation == null || joinedConversation.isEmpty()) {
                    Log.d("ConversationRepository", "Error")
                    callback(true, null, emptyList())
                    return@addOnSuccessListener
                }

                val conversationList = mutableListOf<Conversation>()
                var loadedCount = 0

                for (conversationId in joinedConversation) {
                    val conversationCollection = db.collection("conversations")
                    conversationCollection.whereEqualTo("id", conversationId)
                        .get()
                        .addOnSuccessListener { documents ->
                            for (document in documents) {
                                val id = document.id
                                val createdAt = document.getLong("createdAt") ?: 0L
                                val lastMessage = document.getString("lastMessage") ?: ""
                                val lastSendTime = document.getLong("lastSendTime") ?: 0L
                                val participantIds = document.get("participantIds") as? List<String> ?: listOf()
                                val deleted = document.getBoolean("deleted") ?: false
                                val conversationName = document.getString("conversationName") ?: ""
                                val createBy = document.getString("createBy") ?: ""

                                Log.d("ConversationID", "userUid = $id")

                                val conversation = Conversation(
                                    id = id,
                                    createdAt = createdAt,
                                    participantIds = participantIds,
                                    lastMessage = lastMessage,
                                    lastSendTime = lastSendTime,
                                    deleted = deleted,
                                    conversationName = conversationName,
                                    createBy = createBy
                                )
                                conversationList.add(conversation)
                            }

                            loadedCount++
                            if (loadedCount == joinedConversation.size) {
                                // Sắp xếp danh sách theo lastSendTime giảm dần
                                val sortedList = conversationList.sortedByDescending { it.lastSendTime }
                                callback(true, null, sortedList)
                            }
                        }
                        .addOnFailureListener { e ->
                            callback(false, e.message, null)
                        }
                }
            }
            .addOnFailureListener { e ->
                callback(false, e.message, null)
            }
    }
}