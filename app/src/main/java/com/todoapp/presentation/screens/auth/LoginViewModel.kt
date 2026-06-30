package com.todoapp.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todoapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, error = null) }
            }
            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, error = null) }
            }
            is LoginEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = event.confirmPassword, error = null) }
            }
            is LoginEvent.DisplayNameChanged -> {
                _state.update { it.copy(displayName = event.displayName, error = null) }
            }
            LoginEvent.ToggleMode -> {
                _state.update { it.copy(isSignUpMode = !it.isSignUpMode, error = null) }
            }
            LoginEvent.TogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            LoginEvent.SignIn -> signIn()
            LoginEvent.SignUp -> signUp()
            LoginEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun signIn() {
        if (!validateSignIn()) return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = authRepository.signInWithEmail(_state.value.email, _state.value.password)
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                },
            )
        }
    }

    private fun signUp() {
        if (!validateSignUp()) return

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = authRepository.signUpWithEmail(_state.value.email, _state.value.password)
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false, isSuccess = true) }
                },
                onFailure = { error ->
                    _state.update { it.copy(isLoading = false, error = error.message) }
                },
            )
        }
    }

    private fun validateSignIn(): Boolean {
        if (_state.value.email.isBlank()) {
            _state.update { it.copy(error = "Email cannot be empty") }
            return false
        }
        if (_state.value.password.isEmpty()) {
            _state.update { it.copy(error = "Password cannot be empty") }
            return false
        }
        return true
    }

    private fun validateSignUp(): Boolean {
        if (_state.value.displayName.isBlank()) {
            _state.update { it.copy(error = "Display Name cannot be empty") }
            return false
        }
        if (_state.value.email.isBlank()) {
            _state.update { it.copy(error = "Email cannot be empty") }
            return false
        }
        if (_state.value.password.length < 6) {
            _state.update { it.copy(error = "Password must be at least 6 characters") }
            return false
        }
        if (_state.value.password != _state.value.confirmPassword) {
            _state.update { it.copy(error = "Passwords do not match") }
            return false
        }
        return true
    }
}
