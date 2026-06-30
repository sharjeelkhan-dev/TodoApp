package com.todoapp.presentation.screens.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todoapp.domain.model.SortOrder
import com.todoapp.domain.model.Task
import com.todoapp.domain.model.TaskCategory
import com.todoapp.domain.model.TaskPriority
import com.todoapp.domain.usecase.AddTaskUseCase
import com.todoapp.domain.usecase.DeleteTaskUseCase
import com.todoapp.domain.usecase.GetSmartPrioritizationUseCase
import com.todoapp.domain.usecase.GetTasksUseCase
import com.todoapp.domain.usecase.ProcessAICommandUseCase
import com.todoapp.domain.usecase.SearchTasksUseCase
import com.todoapp.domain.usecase.ToggleTaskCompletionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Task List screen.
 * Handles task retrieval, filtering, searching, deletion, and undo.
 */
@HiltViewModel
class TaskListViewModel @Inject constructor(
    private val getTasksUseCase: GetTasksUseCase,
    private val searchTasksUseCase: SearchTasksUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase,
    private val getSmartPrioritizationUseCase: GetSmartPrioritizationUseCase,
    private val processAICommandUseCase: ProcessAICommandUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TaskListState())
    val state: StateFlow<TaskListState> = _state.asStateFlow()

    private var observeTasksJob: Job? = null

    init {
        observeTasks()
    }

    fun onEvent(event: TaskListEvent) {
        when (event) {
            is TaskListEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                if (event.query.isNotBlank()) {
                    searchTasks(event.query)
                } else {
                    observeTasks()
                }
            }
            TaskListEvent.ToggleSearch -> {
                _state.update { it.copy(isSearchActive = !it.isSearchActive) }
            }
            is TaskListEvent.FilterByStatus -> {
                _state.update {
                    it.copy(filter = it.filter.copy(status = event.isCompleted))
                }
                observeTasks()
            }
            is TaskListEvent.FilterByCategory -> {
                _state.update {
                    it.copy(filter = it.filter.copy(category = event.category))
                }
                observeTasks()
            }
            is TaskListEvent.FilterByPriority -> {
                _state.update {
                    it.copy(filter = it.filter.copy(priority = event.priority))
                }
                observeTasks()
            }
            is TaskListEvent.SortBy -> {
                _state.update {
                    it.copy(filter = it.filter.copy(sortOrder = event.sortOrder))
                }
                observeTasks()
            }
            is TaskListEvent.ToggleCompletion -> {
                viewModelScope.launch {
                    toggleTaskCompletionUseCase(event.taskId)
                }
            }
            is TaskListEvent.DeleteTask -> {
                viewModelScope.launch {
                    _state.update { it.copy(recentlyDeletedTask = event.task) }
                    deleteTaskUseCase(event.task)
                }
            }
            is TaskListEvent.ToggleSubTask -> {
                viewModelScope.launch {
                    val task = _state.value.tasks.find { it.id == event.taskId } ?: return@launch
                    val updatedSubTasks = task.subTasks.map {
                        if (it.id == event.subTaskId) it.copy(isCompleted = !it.isCompleted) else it
                    }
                    val updatedTask = task.copy(subTasks = updatedSubTasks)
                    // We can reuse AddTaskUseCase as it handles upsert
                    addTaskUseCase(updatedTask)
                }
            }
            TaskListEvent.UndoDelete -> {
                viewModelScope.launch {
                    _state.value.recentlyDeletedTask?.let { task ->
                        addTaskUseCase(task)
                        _state.update { it.copy(recentlyDeletedTask = null) }
                    }
                }
            }
            TaskListEvent.ClearDeletedTask -> {
                _state.update { it.copy(recentlyDeletedTask = null) }
            }
            TaskListEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
            TaskListEvent.ToggleFilterSheet -> {
                _state.update { it.copy(showFilterSheet = !it.showFilterSheet) }
            }
            TaskListEvent.RefreshTasks -> observeTasks()
            TaskListEvent.SmartPrioritize -> smartPrioritize()
            TaskListEvent.ToggleAICommandDialog -> {
                _state.update { it.copy(isAICommandDialogOpen = !it.isAICommandDialogOpen) }
            }
            is TaskListEvent.ExecuteAICommand -> executeAICommand(event.prompt)
        }
    }

    private fun executeAICommand(prompt: String) {
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _state.update { it.copy(isAIThinking = true, isAICommandDialogOpen = false, error = null) }
            val result = processAICommandUseCase(prompt)
            _state.update { it.copy(isAIThinking = false) }

            result.onFailure { e ->
                _state.update { it.copy(error = e.message ?: "AI Command failed") }
            }
        }
    }

    private fun smartPrioritize() {
        if (_state.value.tasks.isEmpty()) {
            _state.update { it.copy(error = "Add some tasks first to prioritize!") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isAIThinking = true, error = null) }
            val result = getSmartPrioritizationUseCase()
            _state.update { it.copy(isAIThinking = false) }

            result.onFailure { e ->
                _state.update { it.copy(error = e.message ?: "AI Prioritization failed") }
            }
            
            // Automatically switch to AI_SMART sort order if prioritization succeeded
            result.onSuccess {
                onEvent(TaskListEvent.SortBy(SortOrder.AI_SMART))
            }
        }
    }

    private fun observeTasks() {
        observeTasksJob?.cancel()
        observeTasksJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            getTasksUseCase(_state.value.filter).collectLatest { tasks ->
                _state.update {
                    it.copy(tasks = tasks, isLoading = false)
                }
            }
        }
    }

    private fun searchTasks(query: String) {
        observeTasksJob?.cancel()
        observeTasksJob = viewModelScope.launch {
            searchTasksUseCase(query).collectLatest { tasks ->
                _state.update { it.copy(tasks = tasks) }
            }
        }
    }
}
