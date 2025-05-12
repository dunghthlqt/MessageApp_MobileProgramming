package com.demo.messageapp.repository

import com.demo.messageapp.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MessageRepository {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var messageListener: ListenerRegistration? = null

    fun sendMessage(conversationId: String, userUid: String, content: String, callback: (Boolean, String?) -> Unit) {
        val docRef = db.collection("conversations").document(conversationId)

        docRef.update("lastMessage", content)

        val messageRef = docRef.collection("messages").document()
        val messageId = messageRef.id

        val message = Message(
            id = messageId,
            senderId = userUid,
            content = content,
            timestamp = System.currentTimeMillis(),
            type = "text"
        )

        messageRef.set(message)
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }
    fun deleteMessage(conversationId: String, messageId: String, callback: (Boolean, String?) -> Unit) {
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .document(messageId)
            .update("deleted", true)
            .addOnSuccessListener { _ ->
                callback(true, null)
            }
            .addOnFailureListener { task ->
                callback(false, task.message)
            }
    }
    fun getMessageList(conversationId: String, callback: (Boolean, String?, List<Message>?) -> Unit) {
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .get()
            .addOnSuccessListener { messages ->
                val messageList = mutableListOf<Message>()
                for(message in messages) {
                    val id = message.id
                    val senderId = message.getString("senderId") ?: ""
                    val isSentByMe = message.getBoolean("isSentByMe") ?: false
                    val content = message.getString("content") ?: ""
                    val timestamp = message.getLong("timestamp") ?: 0
                    val type = message.getString("type") ?: ""
                    val deleted = message.getBoolean("deleted") ?: false

                    val message = Message(
                        id = id,
                        senderId = senderId,
                        isSentByMe = isSentByMe,
                        content = content,
                        timestamp = timestamp,
                        type = type,
                        deleted = deleted
                    )
                    messageList.add(message)
                }

                callback(true, null, messageList)
            }
            .addOnFailureListener{ e ->
                callback(false, e.message, null)
            }
    }
    fun searchMessage(conversationId: String, input: String, callback: (Boolean, String?, List<Message>?) -> Unit) {
        db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val messageList = mutableListOf<Message>()
                for(document in querySnapshot) {
                    val id = document.id
                    val senderId = document.getString("senderId") ?: ""
                    val isSentByMe = document.getBoolean("isSentByMe") ?: false
                    val content = document.getString("content") ?: ""
                    val timestamp = document.getLong("timestamp") ?: 0
                    val type = document.getString("type") ?: ""
                    val deleted = document.getBoolean("deleted") ?: false

                    val message = Message(
                        id = id,
                        senderId = senderId,
                        isSentByMe = isSentByMe,
                        content = content,
                        timestamp = timestamp,
                        type = type,
                        deleted = deleted
                    )
                    if (message.content.contains(input, ignoreCase = true)) {
                        messageList.add(message)
                    }
                }

                callback(true, null, messageList)
            }
            .addOnFailureListener{ e ->
                callback(false, e.message, null)
            }
    }
    fun addMessageListener(conversationId: String, callback: (Boolean, String?, List<Message>?) -> Unit) {
        messageListener?.remove()

        messageListener = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) {
                    callback(false, "Snapshot is null", null)
                    return@addSnapshotListener
                }

                val messageList = mutableListOf<Message>()
                for (doc in snapshots.documents) {
                    val id = doc.id
                    val senderId = doc.getString("senderId") ?: ""
                    val isSentByMe = doc.getBoolean("isSentByMe") ?: false
                    val content = doc.getString("content") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: 0
                    val type = doc.getString("type") ?: ""
                    val deleted = doc.getBoolean("deleted") ?: false

                    val message = Message(
                        id = id,
                        senderId = senderId,
                        isSentByMe = isSentByMe,
                        content = content,
                        timestamp = timestamp,
                        type = type,
                        deleted = deleted
                    )

                    messageList.add(message)
                }
                callback(true, null, messageList)
            }
    }
    fun removeMessageListener() {
        messageListener?.remove()
    }
}