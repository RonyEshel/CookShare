package com.cookshare.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.data.remote.api.MealDto
import com.cookshare.data.remote.api.RetrofitClient
import kotlinx.coroutines.launch

class ExternalRecipeDetailViewModel : ViewModel() {

    private val _meal = MutableLiveData<MealDto?>()
    val meal: LiveData<MealDto?> = _meal

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadMeal(mealId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.mealApiService.getMealById(mealId)
                _meal.value = response.meals?.firstOrNull()
            } catch (e: Exception) {
                _error.value = "Failed to load recipe: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
