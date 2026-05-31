package com.todoapp.presentation.screens.settings

import com.todoapp.domain.model.UserProfile

/**
 * UI state for the Settings screen.
 */
data class SettingsState(
    val userProfile: UserProfile? = null,
    val isBackupLoading: Boolean = false,
    val isRestoreLoading: Boolean = false,
    val message: String? = null,
    val isSignedOut: Boolean = false,
    val lastBackupTime: String? = null,
    val lastRestoreTime: String? = null
) {
    // Computed property for general loading if needed
    val isLoading: Boolean get() = isBackupLoading || isRestoreLoading
}

/**
 * Events for the Settings screen.
 */
sealed class SettingsEvent {
    data object SignOut : SettingsEvent()
    data object BackupData : SettingsEvent()
    data object RestoreData : SettingsEvent()
    data object ClearMessage : SettingsEvent()
}
