package com.cookshare.ui.feed

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.data.model.Recipe
import com.cookshare.data.repository.RecipeRepository
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val repository = RecipeRepository()
    val recipes: LiveData<List<Recipe>> = repository.getAllRecipesLocal()

    private val _isRefreshing = MutableLiveData(false)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init { refreshRecipes() }

    fun refreshRecipes() {
        _isRefreshing.value = true
        viewModelScope.launch {
            val result = repository.refreshRecipes()
            _isRefreshing.value = false
            result.onFailure { e -> _error.value = e.message }
        }
    }
}