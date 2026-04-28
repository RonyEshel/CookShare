package com.cookshare.data.model

data class Comment(
    val id: String = "",
    val recipeId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorImage: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
