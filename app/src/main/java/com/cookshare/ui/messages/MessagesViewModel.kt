package com.cookshare.ui.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.data.model.Conversation
import com.cookshare.data.model.User
import com.cookshare.data.remote.firebase.FirebaseManager
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class MessagesViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()

    val currentUserId: String get() = firebaseManager.currentUser?.uid ?: ""
    val currentUserName: String get() = firebaseManager.currentUser?.displayName ?: "Me"
    val currentUserEmail: String get() = firebaseManager.currentUser?.email ?: ""

    private val _conversations = MutableLiveData<List<Conversation>>(emptyList())
    val conversations: LiveData<List<Conversation>> = _conversations

    private val _users = MutableLiveData<List<User>>(emptyList())
    val users: LiveData<List<User>> = _users

    private var listenerReg: ListenerRegistration? = null

    init {
        startListening()
        loadUsers()
    }

    private fun startListening() {
        val uid = currentUserId
        if (uid.isEmpty()) return
        listenerReg = firebaseManager.listenToConversations(uid) { convs ->
            _conversations.postValue(convs)
        }
    }

    private val _startConvResult = MutableLiveData<Result<Pair<String, String>>?>()
    val startConvResult: LiveData<Result<Pair<String, String>>?> = _startConvResult

    fun clearStartConvResult() { _startConvResult.value = null }

    fun startConversation(otherId: String, otherName: String) {
        viewModelScope.launch {
            val result = firebaseManager.getOrCreateConversation(
                currentUserId, currentUserName, otherId, otherName
            )
            _startConvResult.value = result.map { it to otherName }
        }
    }

    private fun loadUsers() {
        viewModelScope.launch {
            val result = firebaseManager.getAllUsers()
            result.onSuccess { userList ->
                _users.value = userList.filter { it.uid != currentUserId }
            }
        }
    }

    override fun onCleared() {
        listenerReg?.remove()
        super.onCleared()
    }
}
