package com.cookshare.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.cookshare.data.model.Recipe
import com.cookshare.data.remote.api.MealDto
import com.cookshare.data.remote.api.RetrofitClient
import com.cookshare.data.repository.RecipeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val recipeRepository = RecipeRepository()

    private val _communityQuery = MutableLiveData("")
    val communityResults: LiveData<List<Recipe>> = _communityQuery.switchMap { query ->
        if (query.isBlank()) MutableLiveData(emptyList())
        else recipeRepository.searchLocalRecipes(query)
    }

    private val _mealDbResults = MutableLiveData<List<MealDto>>(emptyList())
    val mealDbResults: LiveData<List<MealDto>> = _mealDbResults

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var searchJob: Job? = null

    fun searchCommunity(query: String) {
        _communityQuery.value = query
    }

    fun searchMealDb(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _mealDbResults.value = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(400)
            _isLoading.value = true
            try {
                val response = RetrofitClient.mealApiService.searchMeals(query)
                _mealDbResults.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                _error.value = "Search failed: ${e.message}"
                _mealDbResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
