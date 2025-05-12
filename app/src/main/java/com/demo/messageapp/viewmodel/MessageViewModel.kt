package com.demo.messageapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.messageapp.model.Message
import com.demo.messageapp.model.resultmodel.GetMessageListResult
import com.demo.messageapp.model.resultmodel.SearchMessageResult
import com.demo.messageapp.repository.MessageRepository

class MessageViewModel : ViewModel() {
    private val repository = MessageRepository()

    val sendMessageResult = MutableLiveData<Pair<Boolean, String?>>()
    val deleteMessageResult = MutableLiveData<Pair<Boolean, String?>>()
    val getMessageListResult = MutableLiveData<GetMessageListResult>()
    val searchMessageResult = MutableLiveData<SearchMessageResult>()
    val addMessageListenerResult = MutableLiveData<GetMessageListResult>()

    fun sendMessage(conversationId: String, userUid: String, content: String) {

        repository.sendMessage(conversationId, userUid, content) {success, message ->
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
    fun searchMessage(conversationId: String, input: String) {
        repository.searchMessage(conversationId, input) { success, message, messageList ->
            searchMessageResult.value = SearchMessageResult(success, message, messageList)
        }
    }
    fun addMessageListener(conversationId: String, userUid: String) {
        repository.addMessageListener(conversationId) { success, message, messageList ->
            if (messageList != null) {
                for(message in messageList) {
                    if(message.senderId.equals(userUid)) {
                        message.isSentByMe = true
                    }
                }
            }

            addMessageListenerResult.value = GetMessageListResult(success, message, messageList)
        }
    }
    fun removeMessageListener() {
        repository.removeMessageListener()
    }
}