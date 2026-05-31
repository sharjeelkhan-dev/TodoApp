package com.todoapp.domain.usecase

import com.todoapp.domain.model.FilterOption
import com.todoapp.domain.repository.AIRepository
import com.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to trigger AI-based task prioritization and update tasks with recommended scores.
 */
class GetSmartPrioritizationUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val aiRepository: AIRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // 1. Get all tasks to provide context (patterns) to AI
            val tasks = taskRepository.getTasks(FilterOption(status = null)).first()
            
            if (tasks.isEmpty()) return Result.success(Unit)

            // 2. Get scores from AI
            val result = aiRepository.getPrioritizationScores(tasks)
            
            if (result.isSuccess) {
                val scores = result.getOrThrow()
                
                // 3. Update tasks with new scores
                tasks.forEach { task ->
                    val score = scores[task.id]
                    if (score != null) {
                        taskRepository.upsertTask(task.copy(aiPriorityScore = score))
                    }
                }
                Result.success(Unit)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Unknown AI error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
