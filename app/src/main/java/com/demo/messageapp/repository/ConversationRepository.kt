package com.demo.messageapp.repository

import android.util.Log
import com.demo.messageapp.model.Conversation
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ConversationRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun createConversation(participantIds: List<String>, conversationName: String, currentUserId: String, callback: (Boolean, String?, String?) -> Unit) {
        val conversationRef = db.collection("conversations").document()
        val conversationId = conversationRef.id

        fun addConversationIdtoParticipants() {
            val userRef = db.collection("users")
            for(participantId in participantIds) {
                userRef.document(participantId)
                    .update("joinedConversations", FieldValue.arrayUnion(conversationId))
            }
        }

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
                    addConversationIdtoParticipants()
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
                                addConversationIdtoParticipants()
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
        val userRef = db.collection("users")
        val conversationRef = db.collection("conversations")

        conversationRef.document(conversationId)
            .get()
            .addOnSuccessListener { document ->
                val participantIds = document.get("participantIds") as? List<String>

                if (participantIds != null) {
                    for(participantId in participantIds) {
                        userRef.document(participantId)
                            .update("joinedConversations", FieldValue.arrayRemove(conversationId))
                            .addOnFailureListener { e ->
                                callback(false, e.message)
                            }
                    }

                    conversationRef.document(conversationId)
                        .update("deleted", true)
                        .addOnSuccessListener { _ ->
                            callback(true, null)
                        }
                        .addOnFailureListener { e ->
                            callback(false, e.message)
                        }
                }
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }

    fun getConversationList(userUid: String, callback: (Boolean, String?, List<Conversation>?) -> Unit) {
        val userCollection = db.collection("users")

        Log.d("ConversationRepository", "userUid = $userUid")

        userCollection.document(userUid)
            .get()
            .addOnSuccessListener { document ->
                val joinedConversation = document.get("joinedConversations") as? List<String>

                if (joinedConversation.isNullOrEmpty()) {
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
    fun searchConversationByUid(conversationUid: String, callback: (Boolean, String?, Conversation?) -> Unit) {
        db.collection("conversations")
            .whereEqualTo("id", conversationUid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents[0]

                val id = document.id
                val createdAt = document.getLong("createdAt") ?: 0L
                val lastMessage = document.getString("lastMessage") ?: ""
                val lastSendTime = document.getLong("lastSendTime") ?: 0L
                val participantIds = document.get("participantIds") as? List<String> ?: listOf()
                val deleted = document.getBoolean("deleted") ?: false
                val conversationName = document.getString("conversationName") ?: ""
                val createBy = document.getString("createBy") ?: ""

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

                callback(true, null, conversation)
            }
            .addOnFailureListener { e ->
                callback(false, e.message, null)
            }
    }
    fun addConversationListener(userUid: String, callback: (Boolean, String?, List<Conversation>?) -> Unit) {
        val userCollection = db.collection("users")

        Log.d("ConversationRepository", "userUid = $userUid")

        // Lắng nghe thay đổi của tài liệu người dùng
        userCollection.document(userUid)
            .addSnapshotListener { document, userError ->
                if (userError != null) {
                    callback(false, userError.message, null)
                    return@addSnapshotListener
                }

                if (document == null || !document.exists()) {
                    Log.d("ConversationRepository", "Document not found")
                    callback(true, null, emptyList())
                    return@addSnapshotListener
                }

                val joinedConversation = document.get("joinedConversations") as? List<String>

                if (joinedConversation.isNullOrEmpty()) {
                    Log.d("ConversationRepository", "No conversations found")
                    callback(true, null, emptyList())
                    return@addSnapshotListener
                }

                val conversationList = mutableListOf<Conversation>()
                var loadedCount = 0

                // Lắng nghe từng cuộc trò chuyện
                for (conversationId in joinedConversation) {
                    val conversationCollection = db.collection("conversations")
                    conversationCollection.whereEqualTo("id", conversationId)
                        .addSnapshotListener { documents, conversationError ->
                            if (conversationError != null) {
                                callback(false, conversationError.message, null)
                                return@addSnapshotListener
                            }

                            conversationList.removeAll { it.id == conversationId } // Xóa cuộc trò chuyện cũ nếu có

                            for (document in documents!!) {
                                val deleted = document.getBoolean("deleted") ?: false
                                if(!deleted) {
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
                                } else {
                                    break
                                }
                            }

                            loadedCount++
                            if (loadedCount == joinedConversation.size) {
                                // Sắp xếp danh sách theo lastSendTime giảm dần
                                val sortedList = conversationList.sortedByDescending { it.lastSendTime }
                                callback(true, null, sortedList)
                            }
                        }
                }
            }
    }
}