package com.todoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a task stored in the local database.
 * This is the single source of truth for offline-first architecture.
 *
 * @param syncStatus 0 = synced, 1 = pending upload, 2 = pending delete
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
    val dueDate: Long?,          // Stored as epoch millis
    val dueTime: String?,        // "HH:mm" format
    val createdAt: Long,         // Epoch millis
    val updatedAt: Long,         // Epoch millis
    val userId: String,
    val syncStatus: Int = 0      // 0=synced, 1=pendingUpload, 2=pendingDelete
)
