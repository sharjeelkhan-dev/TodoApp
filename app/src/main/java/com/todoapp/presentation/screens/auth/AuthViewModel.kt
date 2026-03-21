package com.todoapp.presentation.screens.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todoapp.domain.repository.AuthRepository
import com.todoapp.domain.usecase.SignInUseCase
import com.todoapp.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Authentication screen.
 * Manages sign-in, sign-up, and Google auth flows.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        // Observe auth state
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _state.update {
                    it.copy(
                        user = user,
                        isSignedIn = user != null
                    )
                }
            }
        }
    }

    fun onEvent(event: AuthEvent) {
        when (event) {
            is AuthEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email, error = null) }
            }
            is AuthEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password, error = null) }
            }
            is AuthEvent.ConfirmPasswordChanged -> {
                _state.update { it.copy(confirmPassword = event.confirmPassword, error = null) }
            }
            AuthEvent.ToggleMode -> {
                _state.update { it.copy(isSignUpMode = !it.isSignUpMode, error = null) }
            }
            AuthEvent.SignIn -> signIn()
            AuthEvent.SignUp -> signUp()
            is AuthEvent.GoogleSignIn -> googleSignIn(event.idToken)
            AuthEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun signIn() {
        val email = _state.value.email.trim()
        if (!isValidEmail(email)) {
            _state.update { it.copy(error = "Please enter a valid email address") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = signInUseCase.withEmail(
                email,
                _state.value.password
            )
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message ?: "Sign in failed")
                    }
                }
            )
        }
    }

    private fun signUp() {
        val currentState = _state.value
        val email = currentState.email.trim()

        if (!isValidEmail(email)) {
            _state.update { it.copy(error = "Please enter a valid email address") }
            return
        }

        if (currentState.password.length < 6) {
            _state.update { it.copy(error = "Password must be at least 6 characters") }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _state.update { it.copy(error = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = signUpUseCase(
                email,
                currentState.password
            )
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message ?: "Sign up failed")
                    }
                }
            )
        }
    }

    private fun googleSignIn(idToken: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = signInUseCase.withGoogle(idToken)
            result.fold(
                onSuccess = {
                    _state.update { it.copy(isLoading = false) }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(isLoading = false, error = error.message ?: "Google sign in failed")
                    }
                }
            )
        }
    }
}
