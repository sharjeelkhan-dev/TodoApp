package com.todoapp.domain.usecase

import com.todoapp.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case for backing up and restoring tasks.
 */
class BackupRestoreUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend fun backup(userId: String): Result<Unit> {
        return try {
            repository.backupTasks(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restore(userId: String): Result<Unit> {
        return try {
            repository.restoreTasks(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
