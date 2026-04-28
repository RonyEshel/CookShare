package com.cookshare.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.cookshare.CookShareApp
import com.cookshare.data.local.dao.RecipeDao
import com.cookshare.data.model.Recipe
import com.cookshare.data.remote.firebase.FirebaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeRepository {

    private val recipeDao: RecipeDao = CookShareApp.instance.database.recipeDao()
    private val firebaseManager = FirebaseManager()

    fun getAllRecipesLocal(): LiveData<List<Recipe>> = recipeDao.getAllRecipes()

    fun getUserRecipesLocal(userId: String): LiveData<List<Recipe>> = recipeDao.getRecipesByUser(userId)

    fun searchLocalRecipes(query: String): LiveData<List<Recipe>> = recipeDao.searchRecipes(query)

    fun getRecipesByCategory(category: String): LiveData<List<Recipe>> = recipeDao.getRecipesByCategory(category)

    suspend fun refreshRecipes(): Result<List<Recipe>> = withContext(Dispatchers.IO) {
        val result = firebaseManager.getAllRecipes()
        result.onSuccess { recipes ->
            recipeDao.deleteAllRecipes()
            recipeDao.insertRecipes(recipes)
        }
        return@withContext result
    }

    suspend fun refreshUserRecipes(userId: String): Result<List<Recipe>> = withContext(Dispatchers.IO) {
        val result = firebaseManager.getRecipesByUser(userId)
        result.onSuccess { recipes ->
            recipes.forEach { recipeDao.insertRecipe(it) }
        }
        return@withContext result
    }

    suspend fun addRecipe(recipe: Recipe, imageUri: Uri?): Result<String> = withContext(Dispatchers.IO) {
        val addResult = firebaseManager.addRecipe(recipe)
        if (addResult.isFailure) return@withContext addResult
        val recipeId = addResult.getOrThrow()
        var finalRecipe = recipe.copy(id = recipeId)
        if (imageUri != null) {
            val uploadResult = firebaseManager.uploadRecipeImage(CookShareApp.instance, imageUri)
            uploadResult.onSuccess { imageUrl ->
                finalRecipe = finalRecipe.copy(imageUrl = imageUrl)
                firebaseManager.updateRecipe(finalRecipe)
            }
        }
        recipeDao.insertRecipe(finalRecipe)
        return@withContext Result.success(recipeId)
    }

    suspend fun updateRecipe(recipe: Recipe, newImageUri: Uri?): Result<Unit> = withContext(Dispatchers.IO) {
        var updatedRecipe = recipe.copy(lastUpdated = System.currentTimeMillis())
        if (newImageUri != null) {
            val uploadResult = firebaseManager.uploadRecipeImage(CookShareApp.instance, newImageUri)
            uploadResult.onSuccess { imageUrl ->
                updatedRecipe = updatedRecipe.copy(imageUrl = imageUrl)
            }
        }
        val result = firebaseManager.updateRecipe(updatedRecipe)
        result.onSuccess { recipeDao.updateRecipe(updatedRecipe) }
        return@withContext result
    }

    suspend fun deleteRecipe(recipeId: String): Result<Unit> = withContext(Dispatchers.IO) {
        val result = firebaseManager.deleteRecipe(recipeId)
        result.onSuccess { recipeDao.deleteRecipeById(recipeId) }
        return@withContext result
    }
}