package com.cookshare.ui.recipe.myrecipes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.data.model.Recipe
import com.cookshare.data.remote.firebase.FirebaseManager
import com.cookshare.data.repository.RecipeRepository
import kotlinx.coroutines.launch

class MyRecipesViewModel : ViewModel() {

    private val recipeRepository = RecipeRepository()
    private val firebaseManager = FirebaseManager()

    val currentUserId: String get() = firebaseManager.currentUser?.uid ?: ""

    lateinit var recipes: LiveData<List<Recipe>>

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun init() {
        val uid = currentUserId
        if (uid.isNotEmpty()) {
            recipes = recipeRepository.getUserRecipesLocal(uid)
            refreshMyRecipes()
        }
    }

    fun refreshMyRecipes() {
        val uid = currentUserId
        if (uid.isEmpty()) return
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = recipeRepository.refreshUserRecipes(uid)
            result.onFailure { _error.value = it.message }
            _isRefreshing.value = false
        }
    }
}
