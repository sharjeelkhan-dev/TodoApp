package com.todoapp.presentation.screens.auth

/**
 * UI state for the Login screen.
 */
data class LoginState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val displayName: String = "",
    val isSignUpMode: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val isPasswordVisible: Boolean = false
)

/**
 * Events for the Login screen.
 */
sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    data class ConfirmPasswordChanged(val confirmPassword: String) : LoginEvent()
    data class DisplayNameChanged(val displayName: String) : LoginEvent()
    data object ToggleMode : LoginEvent()
    data object TogglePasswordVisibility : LoginEvent()
    data object SignIn : LoginEvent()
    data object SignUp : LoginEvent()
    data object ClearError : LoginEvent()
}
