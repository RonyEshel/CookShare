package com.cookshare.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)