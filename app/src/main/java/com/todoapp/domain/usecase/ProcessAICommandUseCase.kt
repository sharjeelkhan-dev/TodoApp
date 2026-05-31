package com.todoapp.domain.usecase

import com.todoapp.domain.model.AIAction
import com.todoapp.domain.model.FilterOption
import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.AIRepository
import com.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to process a natural language AI command.
 */
class ProcessAICommandUseCase @Inject constructor(
    private val aiRepository: AIRepository,
    private val taskRepository: TaskRepository,
    private val addTaskUseCase: AddTaskUseCase,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase
) {
    suspend operator fun invoke(prompt: String): Result<Unit> {
        return try {
            // 1. Get current tasks for context
            val currentTasks = taskRepository.getTasks(FilterOption(status = null)).first()

            // 2. Parse command via AI
            val result = aiRepository.processTaskCommand(prompt, currentTasks)
            
            if (result.isSuccess) {
                val actions = result.getOrThrow()
                
                // 3. Apply actions
                actions.forEach { action ->
                    when (action) {
                        is AIAction.Add -> {
                            addTaskUseCase(Task(
                                title = action.title,
                                description = action.description,
                                priority = action.priority,
                                category = action.category,
                                dueDate = action.dueDate
                            ))
                        }
                        is AIAction.Update -> {
                            val task = currentTasks.find { it.id == action.taskId }
                            if (task != null) {
                                val updatedTask = task.copy(
                                    title = action.title ?: task.title,
                                    description = action.description ?: task.description,
                                    priority = action.priority ?: task.priority,
                                    category = action.category ?: task.category,
                                    dueDate = action.dueDate ?: task.dueDate
                                )
                                updateTaskUseCase(updatedTask)
                            }
                        }
                        is AIAction.Delete -> {
                            val task = currentTasks.find { it.id == action.taskId }
                            if (task != null) {
                                deleteTaskUseCase(task)
                            }
                        }
                        is AIAction.ToggleCompletion -> {
                            val task = currentTasks.find { it.id == action.taskId }
                            if (task != null && task.isCompleted != action.isCompleted) {
                                toggleTaskCompletionUseCase(action.taskId)
                            }
                        }
                    }
                }
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("AI parsing failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
