package com.todoapp.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todoapp.domain.repository.AuthRepository
import com.todoapp.domain.usecase.BackupRestoreUseCase
import com.todoapp.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Settings screen.
 * Handles sign-out, backup, and restore operations.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val signOutUseCase: SignOutUseCase,
    private val backupRestoreUseCase: BackupRestoreUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.SignOut -> {
                viewModelScope.launch {
                    signOutUseCase()
                    _state.update { it.copy(isSignedOut = true) }
                }
            }
            SettingsEvent.BackupData -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    val userId = authRepository.currentUserId
                    if (userId != null) {
                        val result = backupRestoreUseCase.backup(userId)
                        result.fold(
                            onSuccess = {
                                _state.update {
                                    it.copy(isLoading = false, message = "Backup completed successfully!")
                                }
                            },
                            onFailure = { error ->
                                _state.update {
                                    it.copy(isLoading = false, message = "Backup failed: ${error.message}")
                                }
                            }
                        )
                    } else {
                        _state.update {
                            it.copy(isLoading = false, message = "Please sign in to backup")
                        }
                    }
                }
            }
            SettingsEvent.RestoreData -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    val userId = authRepository.currentUserId
                    if (userId != null) {
                        val result = backupRestoreUseCase.restore(userId)
                        result.fold(
                            onSuccess = {
                                _state.update {
                                    it.copy(isLoading = false, message = "Data restored successfully!")
                                }
                            },
                            onFailure = { error ->
                                _state.update {
                                    it.copy(isLoading = false, message = "Restore failed: ${error.message}")
                                }
                            }
                        )
                    } else {
                        _state.update {
                            it.copy(isLoading = false, message = "Please sign in to restore")
                        }
                    }
                }
            }
            SettingsEvent.ClearMessage -> {
                _state.update { it.copy(message = null) }
            }
        }
    }
}
