package com.todoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.todoapp.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the tasks table.
 * Provides all CRUD and query operations for tasks.
 */
@Dao
interface TaskDao {

    // ─── Observe Queries ──────────────────────────────

    /** Observe all tasks ordered by creation date descending. */
    @Query("SELECT * FROM tasks WHERE syncStatus != 2 ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    /** Observe a single task by ID. */
    @Query("SELECT * FROM tasks WHERE id = :taskId AND syncStatus != 2")
    fun getTaskById(taskId: String): Flow<TaskEntity?>

    /** Search tasks by title or description (case-insensitive). */
    @Query("""
        SELECT * FROM tasks 
        WHERE syncStatus != 2 
        AND (title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY createdAt DESC
    """)
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    // ─── Filtered Queries ──────────────────────────────

    /** Get tasks filtered by completion status. */
    @Query("SELECT * FROM tasks WHERE syncStatus != 2 AND isCompleted = :isCompleted ORDER BY createdAt DESC")
    fun getTasksByStatus(isCompleted: Boolean): Flow<List<TaskEntity>>

    /** Get tasks filtered by category. */
    @Query("SELECT * FROM tasks WHERE syncStatus != 2 AND category = :category ORDER BY createdAt DESC")
    fun getTasksByCategory(category: String): Flow<List<TaskEntity>>

    /** Get tasks filtered by priority. */
    @Query("SELECT * FROM tasks WHERE syncStatus != 2 AND priority = :priority ORDER BY createdAt DESC")
    fun getTasksByPriority(priority: String): Flow<List<TaskEntity>>

    // ─── Sorted Queries ──────────────────────────────

    @Query("SELECT * FROM tasks WHERE syncStatus != 2 ORDER BY createdAt ASC")
    fun getTasksByCreatedAsc(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE syncStatus != 2 ORDER BY dueDate ASC")
    fun getTasksByDueDateAsc(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE syncStatus != 2 ORDER BY dueDate DESC")
    fun getTasksByDueDateDesc(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE syncStatus != 2 ORDER BY priority DESC")
    fun getTasksByPriorityHighFirst(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE syncStatus != 2 ORDER BY priority ASC")
    fun getTasksByPriorityLowFirst(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE syncStatus != 2 ORDER BY title ASC")
    fun getTasksAlphabetical(): Flow<List<TaskEntity>>

    // ─── Write Operations ──────────────────────────────

    /** Insert or update a task. Uses UPSERT (insert or replace). */
    @Upsert
    suspend fun upsertTask(task: TaskEntity)

    /** Insert multiple tasks (used for sync/restore). */
    @Upsert
    suspend fun upsertTasks(tasks: List<TaskEntity>)

    /** Soft-delete a task by marking syncStatus = 2. */
    @Query("UPDATE tasks SET syncStatus = 2, updatedAt = :now WHERE id = :taskId")
    suspend fun softDeleteTask(taskId: String, now: Long)

    /** Hard-delete a task (used after cloud sync confirms deletion). */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun hardDeleteTask(taskId: String)

    /** Toggle completion status. */
    @Query("UPDATE tasks SET isCompleted = NOT isCompleted, syncStatus = 1, updatedAt = :now WHERE id = :taskId")
    suspend fun toggleCompletion(taskId: String, now: Long)

    // ─── Sync Operations ──────────────────────────────

    /** Get all tasks pending upload to cloud. */
    @Query("SELECT * FROM tasks WHERE syncStatus = 1")
    suspend fun getPendingUploadTasks(): List<TaskEntity>

    /** Get all tasks pending deletion from cloud. */
    @Query("SELECT * FROM tasks WHERE syncStatus = 2")
    suspend fun getPendingDeleteTasks(): List<TaskEntity>

    /** Mark a task as synced. */
    @Query("UPDATE tasks SET syncStatus = 0 WHERE id = :taskId")
    suspend fun markAsSynced(taskId: String)

    /** Get all tasks for a specific user (for backup). */
    @Query("SELECT * FROM tasks WHERE userId = :userId AND syncStatus != 2")
    suspend fun getTasksByUser(userId: String): List<TaskEntity>

    /** Delete all tasks (for restore). */
    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()
}
