package com.cookshare.ui.recipe.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.CookShareApp
import com.cookshare.data.model.Comment
import com.cookshare.data.model.Recipe
import com.cookshare.data.model.User
import com.cookshare.data.remote.firebase.FirebaseManager
import com.cookshare.data.repository.RecipeRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class RecipeDetailViewModel : ViewModel() {

    private val recipeRepository = RecipeRepository()
    private val firebaseManager = FirebaseManager()

    private val _recipe = MutableLiveData<Recipe?>()
    val recipe: LiveData<Recipe?> = _recipe

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _deleteResult = MutableLiveData<Result<Unit>?>()
    val deleteResult: LiveData<Result<Unit>?> = _deleteResult

    private val _comments = MutableLiveData<List<Comment>>(emptyList())
    val comments: LiveData<List<Comment>> = _comments

    private val _liveUsers = MutableLiveData<Map<String, User>>(emptyMap())
    val liveUsers: LiveData<Map<String, User>> = _liveUsers

    private val _conversationReady = MutableLiveData<Pair<String, String>?>()
    val conversationReady: LiveData<Pair<String, String>?> = _conversationReady

    private val _isFollowing = MutableLiveData<Boolean?>(null)
    val isFollowing: LiveData<Boolean?> = _isFollowing

    val currentUserId: String get() = firebaseManager.currentUser?.uid ?: ""
    val currentUserName: String get() = firebaseManager.currentUser?.displayName ?: "User"

    private var commentsListener: ListenerRegistration? = null

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val cached = CookShareApp.instance.database.recipeDao().getRecipeById(recipeId)
            if (cached != null) { _recipe.value = cached }
            _isLoading.value = false
        }
        commentsListener?.remove()
        commentsListener = firebaseManager.listenToComments(recipeId) { list ->
            _comments.postValue(list)
            // Refresh live user data for everyone in this comment list
            viewModelScope.launch {
                val ids = list.map { it.authorId }.toSet() + setOfNotNull(_recipe.value?.authorId)
                val map = mutableMapOf<String, User>()
                ids.filter { it.isNotEmpty() }.forEach { uid ->
                    firebaseManager.getUserProfile(uid).getOrNull()?.let { map[uid] = it }
                }
                _liveUsers.postValue(map)
            }
        }
    }

    fun postComment(recipeId: String, text: String) {
        val uid = currentUserId
        val name = currentUserName
        if (uid.isEmpty() || text.isBlank()) return
        viewModelScope.launch {
            val image = firebaseManager.getUserProfile(uid).getOrNull()?.profileImageUrl ?: ""
            firebaseManager.addComment(
                Comment(recipeId = recipeId, authorId = uid, authorName = name, authorImage = image, text = text.trim())
            )
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _deleteResult.value = recipeRepository.deleteRecipe(recipeId)
            _isLoading.value = false
        }
    }

    fun checkFollowStatus(authorId: String) {
        viewModelScope.launch {
            _isFollowing.value = firebaseManager.isFollowing(authorId)
        }
    }

    fun toggleFollow(authorId: String) {
        val current = _isFollowing.value ?: return
        viewModelScope.launch {
            if (current) {
                firebaseManager.unfollowUser(authorId)
                _isFollowing.value = false
            } else {
                firebaseManager.followUser(authorId)
                _isFollowing.value = true
            }
        }
    }

    fun startConversationWithAuthor(authorId: String, authorName: String) {
        val myId = currentUserId
        val myName = currentUserName
        if (myId.isEmpty() || myId == authorId) return
        viewModelScope.launch {
            val result = firebaseManager.getOrCreateConversation(myId, myName, authorId, authorName)
            result.onSuccess { convId -> _conversationReady.postValue(Pair(convId, authorName)) }
        }
    }

    override fun onCleared() {
        commentsListener?.remove()
        super.onCleared()
    }
}
