package com.demo.messageapp.repository

import com.demo.messageapp.model.Conversation
import com.google.firebase.firestore.FirebaseFirestore

class ConversationRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

//    fun createConversation(participantIds: List<String>, callback: (Boolean, String?, String?) -> Unit) {
//        val newConversation = Conversation(
//            participantIds = participantIds,
//            createdAt = System.currentTimeMillis(),
//        )
//
//        db.collection("conversations")
//            .add(newConversation)
//            .addOnSuccessListener { docRef ->
//                callback(true, null, docRef.id)
//            }
//            .addOnFailureListener { e ->
//                callback(false, e.message, null)
//            }
//    }
    fun createConversation(participantIds: List<String>, callback: (Boolean, String?, String?) -> Unit) {
        val conversationRef = db.collection("conversations")
                                .document()

        val conversationId = conversationRef.id

        val conversation = Conversation(
            id = conversationId,
            createdAt = System.currentTimeMillis(),
            participantIds = participantIds
        )

        conversationRef.set(conversation)
            .addOnSuccessListener { _ ->
                callback(true, null, conversationId)
            }
            .addOnFailureListener { e ->
                callback(false, e.message, null)
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

        userCollection.document(userUid)
            .get()
            .addOnSuccessListener { document ->
                val joinedConversation = document.get("joinedConversation") as? List<String>

                if (joinedConversation == null || joinedConversation.isEmpty()) {
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
                                val participantIds = document.get("participantIds") as? List<String> ?: listOf()
                                val deleted = document.getBoolean("deleted") ?: false

                                val conversation = Conversation(
                                    id = id,
                                    createdAt = createdAt,
                                    participantIds = participantIds,
                                    lastMessage = lastMessage,
                                    deleted = deleted
                                )
                                conversationList.add(conversation)
                            }

                            loadedCount++
                            if (loadedCount == joinedConversation.size) {
                                callback(true, null, conversationList)
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