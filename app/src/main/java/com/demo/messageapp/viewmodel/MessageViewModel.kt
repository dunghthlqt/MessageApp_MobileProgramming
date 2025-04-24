package com.demo.messageapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.messageapp.model.Message
import com.demo.messageapp.model.resultmodel.GetMessageListResult
import com.demo.messageapp.repository.MessageRepository

class MessageViewModel : ViewModel() {
    private val repository = MessageRepository()

    val sendMessageResult = MutableLiveData<Pair<Boolean, String?>>()
    val deleteMessageResult = MutableLiveData<Pair<Boolean, String?>>()
    val getMessageListResult = MutableLiveData<GetMessageListResult>()


    fun sendMessage(conversationId: String, message: Message) {
        repository.sendMessage(conversationId, message) {success, message ->
            sendMessageResult.value = Pair(success, message)
        }
    }
    fun deleteMessage(conversationId: String, messageId: String) {
        repository.deleteMessage(conversationId, messageId) { success, message ->
            deleteMessageResult.value = Pair(success, message)
        }
    }
    fun getMessageList(conversationId: String) {
        repository.getMessageList(conversationId) {success, message, messageList ->
            getMessageListResult.value = GetMessageListResult(success, message, messageList)
        }
    }
}