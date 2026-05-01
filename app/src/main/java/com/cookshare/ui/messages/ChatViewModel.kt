package com.cookshare.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.data.model.Message
import com.cookshare.data.remote.firebase.FirebaseManager
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()

    val currentUserId: String get() = firebaseManager.currentUser?.uid ?: ""
    val currentUserName: String get() = firebaseManager.currentUser?.displayName ?: "Me"

    private val _messages = MutableLiveData<List<Message>>(emptyList())
    val messages: LiveData<List<Message>> = _messages

    private val _isSending = MutableLiveData(false)
    val isSending: LiveData<Boolean> = _isSending

    private var listenerReg: ListenerRegistration? = null
    private var conversationId: String = ""

    fun init(convId: String) {
        if (conversationId == convId) return
        conversationId = convId
        listenerReg?.remove()
        listenerReg = firebaseManager.listenToMessages(convId) { msgs ->
            _messages.postValue(msgs)
        }
    }

    fun sendMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || conversationId.isEmpty()) return
        val message = Message(
            conversationId = conversationId,
            senderId = currentUserId,
            senderName = currentUserName,
            text = trimmed,
            timestamp = System.currentTimeMillis()
        )
        _isSending.value = true
        viewModelScope.launch {
            firebaseManager.sendMessage(conversationId, message)
            _isSending.value = false
        }
    }

    override fun onCleared() {
        listenerReg?.remove()
        super.onCleared()
    }
}
