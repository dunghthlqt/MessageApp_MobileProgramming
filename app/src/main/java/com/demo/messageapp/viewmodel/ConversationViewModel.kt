package com.demo.messageapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.demo.messageapp.model.Conversation
import com.demo.messageapp.model.resultmodel.CreateConversationResult
import com.demo.messageapp.model.resultmodel.GetConversationListResult
import com.demo.messageapp.model.resultmodel.GetConversationTwoUIDResult
import com.demo.messageapp.model.resultmodel.SearchConversationResult
import com.demo.messageapp.repository.ConversationRepository

class ConversationViewModel : ViewModel() {
    private val repository = ConversationRepository()

    val createConversationResult = MutableLiveData<CreateConversationResult>()
    val deleteConversationResult = MutableLiveData<Pair<Boolean, String?>>()
    val getConversationListResult = MutableLiveData<GetConversationListResult>()
    val getConversationTwoUidResult = MutableLiveData<GetConversationTwoUIDResult>()
    val searchConversationByUidResult = MutableLiveData<SearchConversationResult>()
    
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
    fun getConversationTwoUID(uid1: String, uid2: String) {
        repository.getConversationList(uid2) {success, message, conversationList ->
            if (conversationList != null) {
                if(conversationList.isNotEmpty()) {
                    val conversation = conversationList.firstOrNull { conversation ->
                        val participantIds = conversation.participantIds ?: emptyList()

                        participantIds.size == 2 &&
                                participantIds.contains(uid1) &&
                                participantIds.contains(uid2)
                    }
                    getConversationTwoUidResult.value = GetConversationTwoUIDResult(success, message, conversation?.id)
                } else {
                    getConversationTwoUidResult.value = GetConversationTwoUIDResult(success, message, "")
                }
            }
        }
    }
    fun resetConversationTwoUidResult() {
        // You should adjust this implementation according to your ConversationResult class structure
        getConversationTwoUidResult.value = GetConversationTwoUIDResult(
            success = false,
            errorMessage = "",
            conversationId = ""
        )
    }
    fun searchConversationByUid(conversationUid: String) {
        repository.searchConversationByUid(conversationUid) {success, message, conversation ->
            searchConversationByUidResult.value = SearchConversationResult(success, message, conversation)
        }
    }
}