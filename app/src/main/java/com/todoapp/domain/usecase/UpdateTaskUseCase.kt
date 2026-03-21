package com.todoapp.domain.usecase

import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case for updating an existing task.
 */
class UpdateTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        if (task.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Task title cannot be empty"))
        }
        return try {
            repository.upsertTask(task.copy(updatedAt = java.util.Date()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
