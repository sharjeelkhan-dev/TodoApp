package com.todoapp.domain.usecase

import com.todoapp.domain.model.AIAction
import com.todoapp.domain.model.FilterOption
import com.todoapp.domain.model.SubTask
import com.todoapp.domain.model.Task
import com.todoapp.domain.repository.AIRepository
import com.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

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
                            val newSubTasks = action.subTasks.map { subTaskTitle ->
                                SubTask(
                                    id = UUID.randomUUID().toString(),
                                    title = subTaskTitle,
                                    isCompleted = false
                                )
                            }
                            addTaskUseCase(
                                Task(
                                    title = action.title,
                                    description = action.description,
                                    priority = action.priority,
                                    category = action.category,
                                    dueDate = action.dueDate,
                                    subTasks = newSubTasks
                                )
                            )
                        }

                        is AIAction.AddSubTask -> {
                            val task = currentTasks.find { it.id == action.taskId }
                            if (task != null) {
                                val newSubTask = SubTask(
                                    id = UUID.randomUUID().toString(),
                                    title = action.subTaskTitle,
                                    isCompleted = false
                                )
                                val updatedSubTasks = task.subTasks + newSubTask
                                updateTaskUseCase(task.copy(subTasks = updatedSubTasks))
                            }
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

                        is AIAction.DeleteMultiple -> {
                            action.taskIds.forEach { id ->
                                val task = currentTasks.find { it.id == id }
                                if (task != null) {
                                    deleteTaskUseCase(task)
                                }
                            }
                        }

                        is AIAction.DeleteSubTask -> {
                            val task = currentTasks.find { it.id == action.taskId }
                            if (task != null) {
                                val updatedSubTasks = task.subTasks.filterNot { it.id == action.subTaskId }
                                updateTaskUseCase(task.copy(subTasks = updatedSubTasks))
                            }
                        }

                        is AIAction.ToggleCompletion -> {
                            val task = currentTasks.find { it.id == action.taskId }
                            if (task != null && task.isCompleted != action.isCompleted) {
                                toggleTaskCompletionUseCase(action.taskId)
                            }
                        }

                        is AIAction.ToggleSubTaskCompletion -> {
                            val task = currentTasks.find { it.id == action.taskId }
                            if (task != null) {
                                val updatedSubTasks = task.subTasks.map { subTask ->
                                    if (subTask.id == action.subTaskId) {
                                        subTask.copy(isCompleted = action.isCompleted)
                                    } else {
                                        subTask
                                    }
                                }
                                updateTaskUseCase(task.copy(subTasks = updatedSubTasks))
                            }
                        }

                        is AIAction.GenerateSubTasks -> {
                            // Can be mapped to auto-generate breakdown logic or handled via UI layer
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