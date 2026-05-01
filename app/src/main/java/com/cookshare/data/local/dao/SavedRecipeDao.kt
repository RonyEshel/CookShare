package com.cookshare.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.cookshare.data.model.SavedRecipe

@Dao
interface SavedRecipeDao {

    @Query("SELECT * FROM saved_recipes WHERE userId = :userId ORDER BY savedAt DESC")
    fun getSavedByUser(userId: String): LiveData<List<SavedRecipe>>

    @Query("SELECT recipeId FROM saved_recipes WHERE userId = :userId")
    fun getSavedRecipeIds(userId: String): LiveData<List<String>>

    @Query("SELECT COUNT(*) FROM saved_recipes WHERE userId = :userId AND recipeId = :recipeId")
    suspend fun isSaved(userId: String, recipeId: String): Int

    @Query("SELECT COUNT(*) FROM saved_recipes WHERE userId = :userId")
    suspend fun countForUser(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun save(savedRecipe: SavedRecipe)

    @Query("DELETE FROM saved_recipes WHERE userId = :userId AND recipeId = :recipeId")
    suspend fun unsave(userId: String, recipeId: String)
}
