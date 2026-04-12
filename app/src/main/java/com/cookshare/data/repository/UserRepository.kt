package com.cookshare.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import com.cookshare.CookShareApp
import com.cookshare.data.local.dao.UserDao
import com.cookshare.data.model.User
import com.cookshare.data.remote.firebase.FirebaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository {

    private val userDao: UserDao = CookShareApp.instance.database.userDao()
    private val firebaseManager = FirebaseManager()

    fun getUserLocal(userId: String): LiveData<User?> = userDao.getUserById(userId)

    suspend fun refreshUser(userId: String): Result<User> = withContext(Dispatchers.IO) {
        val result = firebaseManager.getUserProfile(userId)
        result.onSuccess { user -> userDao.insertUser(user) }
        return@withContext result
    }

    suspend fun updateProfile(user: User, newImageUri: Uri?): Result<Unit> = withContext(Dispatchers.IO) {
        var updatedUser = user.copy(lastUpdated = System.currentTimeMillis())
        if (newImageUri != null) {
            val uploadResult = firebaseManager.uploadProfileImage(newImageUri, user.uid)
            uploadResult.onSuccess { imageUrl ->
                updatedUser = updatedUser.copy(profileImageUrl = imageUrl)
            }
        }
        val result = firebaseManager.updateUserProfile(updatedUser)
        result.onSuccess { userDao.updateUser(updatedUser) }
        return@withContext result
    }
}