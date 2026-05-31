package com.todoapp.domain.model

import java.util.Date

/**
 * Core domain model representing a Task.
 */
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.OTHER,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val isReminderEnabled: Boolean = false,
    val dueDate: Date? = null,
    val dueTime: String? = null,  // "HH:mm" format
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val userId: String = "",
    val isSynced: Boolean = false,
    val aiPriorityScore: Int? = null,
    val subTasks: List<SubTask> = emptyList()
)
