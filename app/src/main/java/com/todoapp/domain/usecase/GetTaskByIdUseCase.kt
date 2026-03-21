package com.todoapp.domain.usecase

import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a single task by its ID.
 */
class GetTaskByIdUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(taskId: String): Flow<Task?> {
        return repository.getTaskById(taskId)
    }
}
