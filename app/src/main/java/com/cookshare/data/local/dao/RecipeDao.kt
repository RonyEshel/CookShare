package com.cookshare.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cookshare.data.model.Recipe

@Dao
interface RecipeDao {

    @Query("SELECT * FROM recipes ORDER BY timestamp DESC")
    fun getAllRecipes(): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE authorId = :userId ORDER BY timestamp DESC")
    fun getRecipesByUser(userId: String): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: String): Recipe?

    @Query("SELECT * FROM recipes WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchRecipes(query: String): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE category = :category ORDER BY timestamp DESC")
    fun getRecipesByCategory(category: String): LiveData<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE authorId IN (:authorIds) ORDER BY timestamp DESC")
    fun getRecipesByAuthors(authorIds: List<String>): LiveData<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<Recipe>)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: String)

    @Query("DELETE FROM recipes")
    suspend fun deleteAllRecipes()
}