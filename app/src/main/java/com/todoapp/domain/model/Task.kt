package com.todoapp.domain.model

import java.util.Date

/**
 * Core domain model representing a Task.
 * This is the single source of truth used across the presentation layer.
 * Mapped to/from [com.todoapp.data.local.entity.TaskEntity] by the repository.
 */
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: TaskCategory = TaskCategory.OTHER,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val dueDate: Date? = null,
    val dueTime: String? = null,  // "HH:mm" format
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val userId: String = "",
    val isSynced: Boolean = false
)
