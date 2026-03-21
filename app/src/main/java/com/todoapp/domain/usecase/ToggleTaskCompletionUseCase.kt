package com.todoapp.domain.usecase

import com.todoapp.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case for toggling a task's completion status.
 */
class ToggleTaskCompletionUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: String): Result<Unit> {
        return try {
            repository.toggleCompletion(taskId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
