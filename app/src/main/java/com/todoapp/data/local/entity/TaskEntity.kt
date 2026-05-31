package com.todoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a task stored in the local database.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val isCompleted: Boolean,
    val isReminderEnabled: Boolean = false,
    val dueDate: Long?,
    val dueTime: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val userId: String,
    val syncStatus: Int = 0,
    val aiPriorityScore: Int? = null,
    val subTasks: String? = null // JSON string of List<SubTask>
)
