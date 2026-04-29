package com.cookshare.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.CookShareApp
import com.cookshare.data.model.Recipe
import com.cookshare.data.model.SavedRecipe
import com.cookshare.data.remote.api.MealDto
import com.cookshare.data.remote.api.RetrofitClient
import com.cookshare.data.remote.firebase.FirebaseManager
import com.cookshare.data.repository.RecipeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val recipeRepository = RecipeRepository()
    private val firebaseManager = FirebaseManager()
    private val savedRecipeDao = CookShareApp.instance.database.savedRecipeDao()

    val currentUserId: String get() = firebaseManager.currentUser?.uid ?: ""

    private val _communityQuery = MutableLiveData("")
    private val _communityCategory = MutableLiveData<String?>(null)

    val communityResults = MediatorLiveData<List<Recipe>>().apply {
        var query = ""
        var category: String? = null

        fun refresh() {
            val src = when {
                query.isNotBlank() -> recipeRepository.searchLocalRecipes(query)
                category != null -> recipeRepository.getRecipesByCategory(category!!)
                else -> recipeRepository.getAllRecipesLocal()
            }
            addSource(src) { value = it }
        }

        addSource(_communityQuery) { q -> query = q; refresh() }
        addSource(_communityCategory) { cat -> category = cat; refresh() }
    }

    fun savedRecipeIds(): LiveData<List<String>> = savedRecipeDao.getSavedRecipeIds(currentUserId)

    private val _mealDbResults = MutableLiveData<List<MealDto>>(emptyList())
    val mealDbResults: LiveData<List<MealDto>> = _mealDbResults

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var searchJob: Job? = null

    init { ensureLocalDataLoaded() }

    private fun ensureLocalDataLoaded() {
        viewModelScope.launch { recipeRepository.refreshRecipes() }
    }

    fun searchCommunity(query: String) { _communityQuery.value = query }

    fun selectCommunityCategory(category: String?) {
        _communityCategory.value = category
        _communityQuery.value = ""
    }

    fun selectGlobalCategory(area: String?) {
        searchJob?.cancel()
        if (area == null) { _mealDbResults.value = emptyList(); return }
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = RetrofitClient.mealApiService.getMealsByArea(area)
                _mealDbResults.value = response.meals ?: emptyList()
            } catch (e: Exception) {
                _error.value = "Failed to load: ${e.message}"
                _mealDbResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchMealDb(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) { _mealDbResults.value = emptyList(); return }
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
