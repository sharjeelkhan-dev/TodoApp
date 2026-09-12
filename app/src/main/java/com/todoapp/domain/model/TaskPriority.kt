package com.todoapp.domain.model


enum class TaskPriority(val label: String, val level: Int) {
    LOW("Low", 0),
    MEDIUM("Medium", 1),
    HIGH("High", 2)
}
