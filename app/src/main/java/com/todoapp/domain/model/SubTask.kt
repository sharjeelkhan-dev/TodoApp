package com.todoapp.domain.model

import java.util.UUID

/**
 * Domain model representing a Sub-task.
 */
data class SubTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val isCompleted: Boolean = false
)
