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

    var pendingImageUri: Uri? = null

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
