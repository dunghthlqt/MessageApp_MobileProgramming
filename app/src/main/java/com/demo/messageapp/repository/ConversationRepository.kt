package com.demo.messageapp.repository

import com.demo.messageapp.model.Conversation
import com.google.firebase.firestore.FirebaseFirestore

class ConversationRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun createConversation(participantIds: List<String>, callback: (Boolean, String?, String?) -> Unit) {
        val newConversation = Conversation(
            participantIds = participantIds,
            createdAt = System.currentTimeMillis(),
        )

        db.collection("conversations")
            .add(newConversation)
            .addOnSuccessListener { docRef ->
                callback(true, null, docRef.id)
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
    fun getConversationList(callback: (Boolean, String?, List<Conversation>?) -> Unit) {
        db.collection("conversations")
            .get()
            .addOnSuccessListener { documents ->
                val conversationList = mutableListOf<Conversation>()
                for(document in documents) {
                    val id = document.id
                    val createdAt = document.getLong("createdAt") ?: 0L
                    val lastMessage = document.getString("lastMessage") ?: ""
                    val participantIds = document.get("participantIds") as List<String>
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

                callback(true, null, conversationList)
            }
            .addOnFailureListener{ e ->
                callback(false, e.message, null)
            }
    }
}