package com.cookshare.ui.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.data.model.User
import com.cookshare.data.remote.firebase.FirebaseManager
import com.cookshare.data.repository.UserRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()
    private val userRepository = UserRepository()

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _saveResult = MutableLiveData<Result<Unit>?>()
    val saveResult: LiveData<Result<Unit>?> = _saveResult

    private val _recipeCount = MutableLiveData(0)
    val recipeCount: LiveData<Int> = _recipeCount

    private val _savedCount = MutableLiveData(0)
    val savedCount: LiveData<Int> = _savedCount

    private val _followingCount = MutableLiveData(0)
    val followingCount: LiveData<Int> = _followingCount

    var pendingImageUri: Uri? = null

    fun loadStats() {
        val uid = firebaseManager.currentUser?.uid ?: return
        viewModelScope.launch {
            firebaseManager.getRecipesByUser(uid).onSuccess { _recipeCount.postValue(it.size) }
            _followingCount.postValue(firebaseManager.getFollowingIds().size)
            try {
                val savedDao = com.cookshare.CookShareApp.instance.database.savedRecipeDao()
                _savedCount.postValue(savedDao.countForUser(uid))
            } catch (_: Exception) {}
        }
    }

    fun loadProfile() {
        val uid = firebaseManager.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val result = userRepository.refreshUser(uid)
            result.onSuccess { _user.value = it }
            result.onFailure {
                val firebaseUser = firebaseManager.currentUser
                _user.value = User(
                    uid = firebaseUser?.uid ?: "",
                    displayName = firebaseUser?.displayName ?: "",
                    email = firebaseUser?.email ?: ""
                )
            }
            _isLoading.value = false
        }
    }

    fun saveProfile(displayName: String) {
        val current = _user.value ?: return
        if (displayName.isBlank()) {
            _saveResult.value = Result.failure(Exception("Display name cannot be empty"))
            return
        }
        val updated = current.copy(displayName = displayName)
        viewModelScope.launch {
            _isLoading.value = true
            val result = userRepository.updateProfile(updated, pendingImageUri)
            result.onSuccess {
                pendingImageUri = null
                userRepository.refreshUser(updated.uid).onSuccess { refreshed -> _user.value = refreshed }
            }
            _saveResult.value = result
            _isLoading.value = false
        }
    }

    fun logout() {
        firebaseManager.logout()
    }

    fun getCurrentUserEmail(): String = firebaseManager.currentUser?.email ?: ""
}
