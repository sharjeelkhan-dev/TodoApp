package com.todoapp.domain.model

/**
 * Represents the priority level of a task.
 * Used for sorting and visual differentiation.
 */
enum class TaskPriority(val label: String, val level: Int) {
    LOW("Low", 0),
    MEDIUM("Medium", 1),
    HIGH("High", 2)
}
