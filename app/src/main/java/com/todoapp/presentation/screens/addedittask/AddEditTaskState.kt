package com.todoapp.presentation.screens.addedittask

import com.todoapp.domain.model.Task
import com.todoapp.domain.model.TaskCategory
import com.todoapp.domain.model.TaskPriority
import java.util.Date

/**
 * UI state for the Add/Edit Task screen.
 */
data class AddEditTaskState(
    val taskId: String? = null,
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.OTHER,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val dueDate: Date? = null,
    val dueTime: String? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false
)

/**
 * Events for the Add/Edit Task screen.
 */
sealed class AddEditTaskEvent {
    data class TitleChanged(val title: String) : AddEditTaskEvent()
    data class DescriptionChanged(val desc: String) : AddEditTaskEvent()
    data class CategoryChanged(val category: TaskCategory) : AddEditTaskEvent()
    data class PriorityChanged(val priority: TaskPriority) : AddEditTaskEvent()
    data class DueDateChanged(val date: Date?) : AddEditTaskEvent()
    data class DueTimeChanged(val time: String?) : AddEditTaskEvent()
    data object ToggleDatePicker : AddEditTaskEvent()
    data object ToggleTimePicker : AddEditTaskEvent()
    data object Save : AddEditTaskEvent()
    data object Delete : AddEditTaskEvent()
    data object ClearError : AddEditTaskEvent()
}
