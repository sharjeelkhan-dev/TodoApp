package com.todoapp.domain.model

import androidx.compose.runtime.Immutable
import java.util.UUID

/**
 * Domain model representing a Sub-task.
 */
@Immutable
data class SubTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val isCompleted: Boolean = false
)
