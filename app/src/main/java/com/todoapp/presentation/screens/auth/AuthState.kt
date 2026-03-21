package com.todoapp.presentation.screens.auth

import com.todoapp.domain.model.UserProfile

/**
 * UI state for the Authentication screen.
 */
data class AuthState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isSignUpMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: UserProfile? = null,
    val isSignedIn: Boolean = false
)

/**
 * Events that can occur on the Auth screen.
 */
sealed class AuthEvent {
    data class EmailChanged(val email: String) : AuthEvent()
    data class PasswordChanged(val password: String) : AuthEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : AuthEvent()
    data object ToggleMode : AuthEvent()
    data object SignIn : AuthEvent()
    data object SignUp : AuthEvent()
    data class GoogleSignIn(val idToken: String) : AuthEvent()
    data object ClearError : AuthEvent()
}
