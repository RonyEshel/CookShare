package com.cookshare.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_recipes",
    indices = [Index(value = ["userId", "recipeId"], unique = true)]
)
data class SavedRecipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val recipeId: String = "",
    val recipeTitle: String = "",
    val recipeImageUrl: String = "",
    val listName: String = "Saved",
    val savedAt: Long = System.currentTimeMillis()
)
