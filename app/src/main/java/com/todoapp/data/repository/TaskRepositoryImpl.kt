package com.todoapp.data.repository

import com.todoapp.data.local.dao.TaskDao
import com.todoapp.data.mapper.toDomainModel
import com.todoapp.data.mapper.toDomainModels
import com.todoapp.data.mapper.toEntity
import com.todoapp.data.remote.FirestoreDataSource
import com.todoapp.domain.model.FilterOption
import com.todoapp.domain.model.SortOrder
import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TaskRepository.
 * Bridges Room (local) and Firestore (remote) data sources.
 * Room is the single source of truth (offline-first).
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val firestoreDataSource: FirestoreDataSource
) : TaskRepository {

    override fun getTasks(filter: FilterOption): Flow<List<Task>> {
        // Get base flow based on sort order
        val baseFlow = when (filter.sortOrder) {
            SortOrder.DATE_CREATED_ASC -> taskDao.getTasksByCreatedAsc()
            SortOrder.DATE_CREATED_DESC -> taskDao.getAllTasks()
            SortOrder.DUE_DATE_ASC -> taskDao.getTasksByDueDateAsc()
            SortOrder.DUE_DATE_DESC -> taskDao.getTasksByDueDateDesc()
            SortOrder.PRIORITY_HIGH_FIRST -> taskDao.getTasksByPriorityHighFirst()
            SortOrder.PRIORITY_LOW_FIRST -> taskDao.getTasksByPriorityLowFirst()
            SortOrder.ALPHABETICAL -> taskDao.getTasksAlphabetical()
        }

        // Apply additional filters in-memory
        return baseFlow.map { entities ->
            entities.toDomainModels().filter { task ->
                val statusMatch = filter.status?.let { task.isCompleted == it } ?: true
                val categoryMatch = filter.category?.let { task.category == it } ?: true
                val priorityMatch = filter.priority?.let { task.priority == it } ?: true
                statusMatch && categoryMatch && priorityMatch
            }
        }
    }

    override fun getTaskById(taskId: String): Flow<Task?> {
        return taskDao.getTaskById(taskId).map { it?.toDomainModel() }
    }

    override fun searchTasks(query: String): Flow<List<Task>> {
        return taskDao.searchTasks(query).map { it.toDomainModels() }
    }

    override suspend fun upsertTask(task: Task) {
        val taskToSave = if (task.id.isBlank()) {
            task.copy(id = UUID.randomUUID().toString())
        } else {
            task
        }
        taskDao.upsertTask(taskToSave.toEntity(syncStatus = 1))
    }

    override suspend fun deleteTask(task: Task) {
        taskDao.softDeleteTask(task.id, System.currentTimeMillis())
    }

    override suspend fun toggleCompletion(taskId: String) {
        taskDao.toggleCompletion(taskId, System.currentTimeMillis())
    }

    override suspend fun syncToCloud() {
        try {
            // Upload pending tasks
            val pendingUploads = taskDao.getPendingUploadTasks()
            for (task in pendingUploads) {
                if (task.userId.isNotBlank()) {
                    firestoreDataSource.uploadTask(task.userId, task)
                    taskDao.markAsSynced(task.id)
                }
            }

            // Delete pending deletions from cloud
            val pendingDeletes = taskDao.getPendingDeleteTasks()
            for (task in pendingDeletes) {
                if (task.userId.isNotBlank()) {
                    firestoreDataSource.deleteTask(task.userId, task.id)
                }
                taskDao.hardDeleteTask(task.id)
            }
        } catch (_: Exception) {
            // Sync failures are silent — will retry on next sync
        }
    }

    override suspend fun syncFromCloud(userId: String) {
        try {
            val remoteTasks = firestoreDataSource.fetchAllTasks(userId)
            taskDao.upsertTasks(remoteTasks)
        } catch (_: Exception) {
            // Sync failures are silent — local data remains valid
        }
    }

    override suspend fun backupTasks(userId: String) {
        val tasks = taskDao.getTasksByUser(userId)
        firestoreDataSource.backupTasks(userId, tasks)
    }

    override suspend fun restoreTasks(userId: String) {
        val restoredTasks = firestoreDataSource.restoreTasks(userId)
        taskDao.deleteAllTasks()
        taskDao.upsertTasks(restoredTasks)
    }
}
