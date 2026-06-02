package com.todoapp.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todoapp.domain.model.FilterOption
import com.todoapp.domain.repository.AuthRepository
import com.todoapp.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Splash Screen.
 * Simplified: Authentication removed. Directly loads real-time task statistics.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _tasksCount = MutableStateFlow(0)
    val tasksCount: StateFlow<Int> = _tasksCount.asStateFlow()

    private val _doneCount = MutableStateFlow(0)
    val doneCount: StateFlow<Int> = _doneCount.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(authRepository.isSignedIn)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    init {
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            // Fetch all tasks using default filter option
            taskRepository.getTasks(FilterOption()).collectLatest { tasks ->
                val totalWorkItems = tasks.size + tasks.sumOf { it.subTasks.size }
                val doneWorkItems = tasks.count { it.isCompleted } + tasks.sumOf { it.subTasks.count { st -> st.isCompleted } }
                
                _tasksCount.value = totalWorkItems
                _doneCount.value = doneWorkItems
            }
        }
    }
}
