package com.example.postalon.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postalon.data.FirebaseService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Loading : AuthState()
    object Ready : AuthState()
    data class Error(val message: String) : AuthState()
}


class AuthViewModel(
    private val firebaseService: FirebaseService
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        initAuth()
    }

    private fun initAuth() {
        viewModelScope.launch {
            try {
                val user = firebaseService.signInAnonymouslyIfNeeded()
                _authState.value = if (user != null) AuthState.Ready
                else AuthState.Error("No login")
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
