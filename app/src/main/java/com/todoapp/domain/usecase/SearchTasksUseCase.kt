package com.todoapp.domain.usecase

import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for searching tasks by query string.
 */
class SearchTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(query: String): Flow<List<Task>> {
        return repository.searchTasks(query)
    }
}
