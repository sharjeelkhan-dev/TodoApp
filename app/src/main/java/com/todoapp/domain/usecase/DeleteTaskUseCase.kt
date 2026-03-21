package com.todoapp.domain.usecase

import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case for deleting a task.
 */
class DeleteTaskUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        return try {
            repository.deleteTask(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
