package com.todoapp.domain.repository

import com.todoapp.domain.model.FilterOption
import com.todoapp.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Task operations.
 * The implementation bridges local (Room) and remote (Firestore) data sources.
 */
interface TaskRepository {

    /** Observe all tasks with optional filtering. */
    fun getTasks(filter: FilterOption): Flow<List<Task>>

    /** Observe a single task by its ID. */
    fun getTaskById(taskId: String): Flow<Task?>

    /** Search tasks by title or description. */
    fun searchTasks(query: String): Flow<List<Task>>

    /** Insert or update a task locally and queue sync. */
    suspend fun upsertTask(task: Task)

    /** Delete a task locally and queue sync. */
    suspend fun deleteTask(task: Task)

    /** Toggle the completion status of a task. */
    suspend fun toggleCompletion(taskId: String)

    /** Sync local changes to the cloud. */
    suspend fun syncToCloud()

    /** Pull latest data from the cloud. */
    suspend fun syncFromCloud(userId: String)

    /** Backup all tasks to Firestore. */
    suspend fun backupTasks(userId: String)

    /** Restore tasks from Firestore. */
    suspend fun restoreTasks(userId: String)
}
