package com.todoapp.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import com.todoapp.data.local.PreferenceManager
import com.todoapp.domain.repository.AuthRepository
import com.todoapp.domain.usecase.BackupRestoreUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * ViewModel for Settings screen.
 * Simplified: Authentication removed. Uses a fixed "default_user" ID for cloud storage.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupRestoreUseCase: BackupRestoreUseCase,
    private val authRepository: AuthRepository,
    private val preferenceManager: PreferenceManager,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    private val userId = authRepository.currentUserId ?: "anonymous"

    init {
        loadUserProfile()
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferenceManager.apiKey.collectLatest { key ->
                _state.update { it.copy(customApiKey = key ?: "") }
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val profile = authRepository.currentUser.first()
            _state.update { it.copy(userProfile = profile) }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.SignOut -> {
                viewModelScope.launch {
                    authRepository.signOut()
                    _state.update { it.copy(isSignedOut = true) }
                }
            }
            SettingsEvent.BackupData -> {
                viewModelScope.launch {
                    try {
                        _state.update { it.copy(isBackupLoading = true, message = null) }
                        
                        withTimeout(1.minutes) {
                            backupRestoreUseCase.backup(userId).fold(
                                onSuccess = {
                                    val currentTime = dateTimeFormat.format(Date())
                                    _state.update { 
                                        it.copy(
                                            message = "Backup complete",
                                            lastBackupTime = "Last backup: $currentTime",
                                            isBackupLoading = false
                                        ) 
                                    }
                                },
                                onFailure = { error ->
                                    val errorMessage = when {
                                        error.message?.contains("No tasks") == true -> "Nothing to backup yet."
                                        else -> "Backup failed: ${error.localizedMessage ?: "Network error"}"
                                    }
                                    _state.update { it.copy(message = errorMessage, isBackupLoading = false) }
                                }
                            )
                        }
                    } catch (_: TimeoutCancellationException) {
                        _state.update { it.copy(message = "Backup timed out. Please check your network.", isBackupLoading = false) }
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        _state.update { it.copy(message = "Connection error. Please try again.", isBackupLoading = false) }
                    } finally {
                        _state.update { it.copy(isBackupLoading = false) }
                    }
                }
            }
            SettingsEvent.RestoreData -> {
                viewModelScope.launch {
                    try {
                        _state.update { it.copy(isRestoreLoading = true, message = null) }
                        
                        withTimeout(10000L) {
                            backupRestoreUseCase.restore(userId).fold(
                                onSuccess = {
                                    val currentTime = dateTimeFormat.format(Date())
                                    _state.update { 
                                        it.copy(
                                            message = "Restore complete",
                                            lastRestoreTime = "Last restore: $currentTime",
                                            isRestoreLoading = false
                                        ) 
                                    }
                                },
                                onFailure = { error ->
                                    val errorMessage = when {
                                        error.message?.contains("No backup") == true -> "No cloud backup found."
                                        else -> "Restore failed: ${error.localizedMessage ?: "Network error"}"
                                    }
                                    _state.update { it.copy(message = errorMessage, isRestoreLoading = false) }
                                }
                            )
                        }
                    } catch (e: TimeoutCancellationException) {
                        _state.update { it.copy(message = "Restore timed out. Please check your network.", isRestoreLoading = false) }
                    } catch (e: Exception) {
                        if (e is kotlinx.coroutines.CancellationException) throw e
                        _state.update { it.copy(message = "Connection error. Please try again.", isRestoreLoading = false) }
                    } finally {
                        _state.update { it.copy(isRestoreLoading = false) }
                    }
                }
            }
            SettingsEvent.ClearMessage -> {
                _state.update { it.copy(message = null) }
            }
            is SettingsEvent.ApiKeyChanged -> {
                _state.update { it.copy(customApiKey = event.key) }
            }
            SettingsEvent.SaveApiKey -> {
                viewModelScope.launch {
                    preferenceManager.saveApiKey(_state.value.customApiKey)
                    _state.update { it.copy(message = "API Key saved successfully", showApiKeyDialog = false) }
                }
            }
            SettingsEvent.ToggleApiKeyDialog -> {
                _state.update { it.copy(showApiKeyDialog = !it.showApiKeyDialog) }
            }
        }
    }
}
