package com.cookshare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val ingredients: String = "",
    val instructions: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val cookingTime: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis()
)