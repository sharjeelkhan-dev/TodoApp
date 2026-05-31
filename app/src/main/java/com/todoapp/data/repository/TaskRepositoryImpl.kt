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
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val firestoreDataSource: FirestoreDataSource
) : TaskRepository {

    override fun getTasks(filter: FilterOption): Flow<List<Task>> {
        val baseFlow = when (filter.sortOrder) {
            SortOrder.DATE_CREATED_ASC -> taskDao.getTasksByCreatedAsc()
            SortOrder.DATE_CREATED_DESC -> taskDao.getAllTasks()
            SortOrder.DUE_DATE_ASC -> taskDao.getTasksByDueDateAsc()
            SortOrder.DUE_DATE_DESC -> taskDao.getTasksByDueDateDesc()
            SortOrder.PRIORITY_HIGH_FIRST -> taskDao.getTasksByPriorityHighFirst()
            SortOrder.PRIORITY_LOW_FIRST -> taskDao.getTasksByPriorityLowFirst()
            SortOrder.ALPHABETICAL -> taskDao.getTasksAlphabetical()
            SortOrder.AI_SMART -> taskDao.getTasksByAISmart()
        }

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
            val pendingUploads = taskDao.getPendingUploadTasks()
            for (task in pendingUploads) {
                // Use default_user if userId is empty
                val uId = if (task.userId.isBlank()) "default_user" else task.userId
                firestoreDataSource.uploadTask(uId, task)
                taskDao.markAsSynced(task.id)
            }

            val pendingDeletes = taskDao.getPendingDeleteTasks()
            for (task in pendingDeletes) {
                val uId = if (task.userId.isBlank()) "default_user" else task.userId
                firestoreDataSource.deleteTask(uId, task.id)
                taskDao.hardDeleteTask(task.id)
            }
        } catch (_: Exception) {}
    }

    override suspend fun syncFromCloud(userId: String) {
        try {
            val remoteTasks = firestoreDataSource.fetchAllTasks(userId)
            taskDao.upsertTasks(remoteTasks)
        } catch (_: Exception) {}
    }

    override suspend fun backupTasks(userId: String) {
        // Fetch all local tasks, ignoring their individual userId fields
        val tasks = taskDao.getAllTasksForBackup()
        if (tasks.isNotEmpty()) {
            // Assign the backup userId to all tasks before uploading
            val tasksWithUserId = tasks.map { it.copy(userId = userId) }
            firestoreDataSource.backupTasks(userId, tasksWithUserId)
        } else {
            throw Exception("No tasks to backup")
        }
    }

    override suspend fun restoreTasks(userId: String) {
        val restoredTasks = firestoreDataSource.restoreTasks(userId)
        if (restoredTasks.isNotEmpty()) {
            // Ensure restored tasks maintain their original sync status (synced)
            val entitiesToRestore = restoredTasks.map { it.copy(syncStatus = 0) }
            
            // Perform delete and insert in a single step
            taskDao.deleteAllTasks()
            taskDao.upsertTasks(entitiesToRestore)
        } else {
            throw Exception("No backup found to restore")
        }
    }
}
