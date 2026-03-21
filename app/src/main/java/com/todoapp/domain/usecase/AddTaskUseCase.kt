package com.todoapp.domain.usecase

import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case for adding a new task.
 * Validates input before delegating to the repository.
 */
class AddTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        // Validate required fields
        if (task.title.isBlank()) {
            return Result.failure(IllegalArgumentException("Task title cannot be empty"))
        }
        return try {
            repository.upsertTask(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
