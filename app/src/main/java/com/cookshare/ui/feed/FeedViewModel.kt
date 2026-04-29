package com.cookshare.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.CookShareApp
import com.cookshare.data.SeedData
import com.cookshare.data.model.Recipe
import com.cookshare.data.model.SavedRecipe
import com.cookshare.data.model.User
import com.cookshare.data.remote.firebase.FirebaseManager
import com.cookshare.data.repository.RecipeRepository
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val repository = RecipeRepository()
    private val firebaseManager = FirebaseManager()
    private val savedRecipeDao = CookShareApp.instance.database.savedRecipeDao()

    val currentUserId: String get() = firebaseManager.currentUser?.uid ?: ""

    private val allRecipes: LiveData<List<Recipe>> = repository.getAllRecipesLocal()
    private val _feedMode = MutableLiveData("everyone")
    private val _followingIds = MutableLiveData<List<String>>(emptyList())

    val displayRecipes = MediatorLiveData<List<Recipe>>().apply {
        var mode = "everyone"
        var followingIds = emptySet<String>()
        var cachedAll = emptyList<Recipe>()

        addSource(allRecipes) { recipes ->
            cachedAll = recipes ?: emptyList()
            value = if (mode == "following") cachedAll.filter { it.authorId in followingIds }
                    else cachedAll
        }
        addSource(_feedMode) { m ->
            mode = m ?: "everyone"
            value = if (mode == "following") cachedAll.filter { it.authorId in followingIds }
                    else cachedAll
        }
        addSource(_followingIds) { ids ->
            followingIds = ids?.toSet() ?: emptySet()
            if (mode == "following") value = cachedAll.filter { it.authorId in followingIds }
        }
    }

    fun setFeedMode(mode: String) {
        _feedMode.value = mode
        if (mode == "following") loadFollowingIds()
    }

    fun loadFollowingIds() {
        viewModelScope.launch {
            _followingIds.value = firebaseManager.getFollowingIds()
        }
    }

    fun savedRecipeIds(): LiveData<List<String>> = savedRecipeDao.getSavedRecipeIds(currentUserId)

    private val _liveUsers = MutableLiveData<Map<String, User>>(emptyMap())
    val liveUsers: LiveData<Map<String, User>> = _liveUsers

    fun refreshLiveUsers() {
        viewModelScope.launch {
            val authorIds = (allRecipes.value ?: emptyList()).map { it.authorId }.toSet()
            val map = mutableMapOf<String, User>()
            authorIds.filter { it.isNotEmpty() }.forEach { uid ->
                firebaseManager.getUserProfile(uid).getOrNull()?.let { map[uid] = it }
            }
            _liveUsers.value = map
        }
    }

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init { refreshRecipes(seedIfEmpty = true) }

    fun refreshRecipes() { refreshRecipes(seedIfEmpty = false) }

    private fun refreshRecipes(seedIfEmpty: Boolean) {
        _isRefreshing.value = true
        viewModelScope.launch {
            if (seedIfEmpty) SeedData.seedIfNeeded(firebaseManager)
            val result = repository.refreshRecipes()
            _isRefreshing.value = false
            result.onFailure { e -> _error.value = e.message }
        }
    }

    fun toggleSave(recipe: Recipe) {
        val uid = currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            val isSaved = savedRecipeDao.isSaved(uid, recipe.id) > 0
            if (isSaved) {
                savedRecipeDao.unsave(uid, recipe.id)
            } else {
                savedRecipeDao.save(
                    SavedRecipe(
                        userId = uid,
                        recipeId = recipe.id,
                        recipeTitle = recipe.title,
                        recipeImageUrl = recipe.imageUrl
                    )
                )
            }
        }
    }
}
