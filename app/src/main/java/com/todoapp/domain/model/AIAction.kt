package com.todoapp.domain.model

import java.util.Date

/**
 * Represents an action parsed by the AI from a user prompt.
 */
sealed class AIAction {
    data class Add(
        val title: String,
        val description: String = "",
        val priority: TaskPriority = TaskPriority.MEDIUM,
        val category: TaskCategory = TaskCategory.OTHER,
        val dueDate: Date? = null
    ) : AIAction()

    data class Update(
        val taskId: String,
        val title: String? = null,
        val description: String? = null,
        val priority: TaskPriority? = null,
        val category: TaskCategory? = null,
        val dueDate: Date? = null
    ) : AIAction()

    data class Delete(val taskId: String) : AIAction()

    data class ToggleCompletion(val taskId: String, val isCompleted: Boolean) : AIAction()
}
