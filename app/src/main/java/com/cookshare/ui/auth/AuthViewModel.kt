package com.cookshare.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookshare.data.remote.firebase.FirebaseManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val firebaseManager = FirebaseManager()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        if (firebaseManager.isLoggedIn) {
            _authState.value = AuthState.Authenticated(firebaseManager.currentUser!!)
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val result = firebaseManager.login(email, password)
            _isLoading.value = false
            result.onSuccess { user -> _authState.value = AuthState.Authenticated(user) }
                .onFailure { e -> _authState.value = AuthState.Error(e.message ?: "Login failed") }
        }
    }

    fun register(email: String, password: String, displayName: String) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }
        if (password.length < 6) {
            _authState.value = AuthState.Error("Password must be at least 6 characters")
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            val result = firebaseManager.register(email, password, displayName)
            _isLoading.value = false
            result.onSuccess { user -> _authState.value = AuthState.Authenticated(user) }
                .onFailure { e -> _authState.value = AuthState.Error(e.message ?: "Registration failed") }
        }
    }

    fun logout() {
        firebaseManager.logout()
        _authState.value = AuthState.Unauthenticated
    }
}

sealed class AuthState {
    data class Authenticated(val user: FirebaseUser) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}