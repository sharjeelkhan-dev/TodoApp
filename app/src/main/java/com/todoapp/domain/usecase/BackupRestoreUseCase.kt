package com.todoapp.domain.usecase

import com.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class BackupRestoreUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend fun backup(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            repository.backupTasks(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }

    suspend fun restore(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            repository.restoreTasks(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            Result.failure(e)
        }
    }
}
