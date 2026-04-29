package com.cookshare.ui.recipe.edit

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.CookShareApp
import com.cookshare.data.model.Recipe
import com.cookshare.data.remote.firebase.FirebaseManager
import com.cookshare.data.repository.RecipeRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class EditRecipeViewModel : ViewModel() {

    private val repository = RecipeRepository()
    private val firebaseManager = FirebaseManager()
    private val recipeDao = CookShareApp.instance.database.recipeDao()

    private val _recipe = MutableLiveData<Recipe?>()
    val recipe: LiveData<Recipe?> = _recipe

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveResult = MutableLiveData<Result<String>?>()
    val saveResult: LiveData<Result<String>?> = _saveResult

    var imageUri: Uri? = null
    var isEditMode = false

    fun loadRecipe(recipeId: String?) {
        if (recipeId.isNullOrEmpty()) { isEditMode = false; return }
        isEditMode = true
        viewModelScope.launch {
            _recipe.value = recipeDao.getRecipeById(recipeId)
        }
    }

    fun saveRecipe(title: String, description: String, ingredients: String, instructions: String, category: String, cookingTime: Int) {
        if (title.isBlank() || description.isBlank()) {
            _saveResult.value = Result.failure(Exception("Title and description are required"))
            return
        }
        _isLoading.value = true
        val currentUser = FirebaseAuth.getInstance().currentUser

        viewModelScope.launch {
            if (isEditMode && _recipe.value != null) {
                val updated = _recipe.value!!.copy(
                    title = title, description = description, ingredients = ingredients,
                    instructions = instructions, category = category, cookingTime = cookingTime,
                    lastUpdated = System.currentTimeMillis()
                )
                val result = repository.updateRecipe(updated, imageUri)
                _isLoading.value = false
                _saveResult.value = result.map { updated.id }
            } else {
                val authName = currentUser?.displayName.orEmpty()
                val resolvedName = if (authName.isNotBlank()) authName else {
                    val uid = currentUser?.uid.orEmpty()
                    if (uid.isNotEmpty()) firebaseManager.getUserProfile(uid).getOrNull()?.displayName.orEmpty() else ""
                }.ifBlank { currentUser?.email?.substringBefore("@").orEmpty() }.ifBlank { "Anonymous" }

                val newRecipe = Recipe(
                    title = title, description = description, ingredients = ingredients,
                    instructions = instructions, category = category, cookingTime = cookingTime,
                    authorId = currentUser?.uid ?: "",
                    authorName = resolvedName,
                    timestamp = System.currentTimeMillis()
                )
                val result = repository.addRecipe(newRecipe, imageUri)
                _isLoading.value = false
                _saveResult.value = result
            }
        }
    }
}