package com.todoapp.presentation.screens.tasklist

import com.todoapp.domain.model.FilterOption
import com.todoapp.domain.model.SortOrder
import com.todoapp.domain.model.Task
import com.todoapp.domain.model.TaskCategory
import com.todoapp.domain.model.TaskPriority

/**
 * UI state for the Task List screen.
 */
data class TaskListState(
    val tasks: List<Task> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val filter: FilterOption = FilterOption(),
    val isLoading: Boolean = false,
    val isAIThinking: Boolean = false,
    val isAICommandDialogOpen: Boolean = false,
    val error: String? = null,
    val recentlyDeletedTask: Task? = null,
    val showFilterSheet: Boolean = false,
)

/**
 * Events for the Task List screen.
 */
sealed class TaskListEvent {
    data class SearchQueryChanged(val query: String) : TaskListEvent()
    data object ToggleSearch : TaskListEvent()
    data class FilterByStatus(val isCompleted: Boolean?) : TaskListEvent()
    data class FilterByCategory(val category: TaskCategory?) : TaskListEvent()
    data class FilterByPriority(val priority: TaskPriority?) : TaskListEvent()
    data class SortBy(val sortOrder: SortOrder) : TaskListEvent()
    data class ToggleCompletion(val taskId: String) : TaskListEvent()
    data class DeleteTask(val task: Task) : TaskListEvent()
    data class ToggleSubTask(val taskId: String, val subTaskId: String) : TaskListEvent()
    data object UndoDelete : TaskListEvent()
    data object ClearDeletedTask : TaskListEvent() // New event to clear state
    data object ClearError : TaskListEvent()
    data object ToggleFilterSheet : TaskListEvent()
    data object RefreshTasks : TaskListEvent()
    data object SmartPrioritize : TaskListEvent()
    data object ToggleAICommandDialog : TaskListEvent()
    data class ExecuteAICommand(val prompt: String) : TaskListEvent()
}
