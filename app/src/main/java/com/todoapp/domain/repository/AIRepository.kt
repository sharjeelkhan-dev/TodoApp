package com.todoapp.domain.repository

import com.todoapp.domain.model.AIAction
import com.todoapp.domain.model.Task

/**
 * Repository interface for AI-related operations.
 */
interface AIRepository {
    /**
     * Analyzes tasks and returns a map of task IDs to their AI-recommended priority scores (0-100).
     */
    suspend fun getPrioritizationScores(tasks: List<Task>): Result<Map<String, Int>>

    /**
     * Processes a natural language prompt and returns a list of actions to perform on tasks.
     */
    suspend fun processTaskCommand(prompt: String, currentTasks: List<Task>): Result<List<AIAction>>
}
