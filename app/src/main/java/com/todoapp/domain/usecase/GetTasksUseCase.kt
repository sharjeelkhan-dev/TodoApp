package com.todoapp.domain.usecase

import com.todoapp.domain.model.FilterOption
import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving tasks with filters applied.
 */
class GetTasksUseCase @Inject constructor(
    private val repository: TaskRepository
) {
    operator fun invoke(filter: FilterOption = FilterOption()): Flow<List<Task>> {
        return repository.getTasks(filter)
    }
}
