package com.todoapp.domain.usecase

import com.todoapp.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case for syncing tasks between local and cloud storage.
 */
class SyncTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(userId: String): Result<Unit> {
        return try {
            repository.syncFromCloud(userId)
            repository.syncToCloud()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
