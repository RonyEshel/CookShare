package com.cookshare.ui.recipe.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.CookShareApp
import com.cookshare.data.model.Recipe
import com.cookshare.data.remote.firebase.FirebaseManager
import com.cookshare.data.repository.RecipeRepository
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

    val currentUserId: String get() = firebaseManager.currentUser?.uid ?: ""

    fun loadRecipe(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val cached = CookShareApp.instance.database.recipeDao().getRecipeById(recipeId)
            if (cached != null) {
                _recipe.value = cached
                _isLoading.value = false
            } else {
                _isLoading.value = false
            }
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = recipeRepository.deleteRecipe(recipeId)
            _deleteResult.value = result
            _isLoading.value = false
        }
    }
}
