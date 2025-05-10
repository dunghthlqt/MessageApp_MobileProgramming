package com.demo.messageapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.messageapp.model.resultmodel.CreateConversationResult
import com.demo.messageapp.model.resultmodel.GetConversationListResult
import com.demo.messageapp.repository.ConversationRepository

class ConversationViewModel : ViewModel() {
    private val repository = ConversationRepository()

    val createConversationResult = MutableLiveData<CreateConversationResult>()
    val deleteConversationResult = MutableLiveData<Pair<Boolean, String?>>()
    val getConversationListResult = MutableLiveData<GetConversationListResult>()
    
    fun createConversation(participantIds: List<String>, conversationName: String, currentUserId: String) {
        repository.createConversation(participantIds, conversationName, currentUserId) {success, message, conversationId ->
            createConversationResult.value = CreateConversationResult(success, message, conversationId)
        }
    }
    fun deleteConversation(conversationId: String) {
        repository.deleteConversation(conversationId) { success, message ->
            deleteConversationResult.value = Pair(success, message)
        }
    }
    fun getConversationList(userUid: String) {
        repository.getConversationList(userUid) {success, message, conversationList ->
            getConversationListResult.value = GetConversationListResult(success, message, conversationList)
        }
    }
}