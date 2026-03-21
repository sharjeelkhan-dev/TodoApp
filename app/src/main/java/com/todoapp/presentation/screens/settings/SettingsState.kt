package com.todoapp.presentation.screens.settings

/**
 * UI state for the Settings screen.
 */
data class SettingsState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val isSignedOut: Boolean = false
)

/**
 * Events for the Settings screen.
 */
sealed class SettingsEvent {
    data object SignOut : SettingsEvent()
    data object BackupData : SettingsEvent()
    data object RestoreData : SettingsEvent()
    data object ClearMessage : SettingsEvent()
}
