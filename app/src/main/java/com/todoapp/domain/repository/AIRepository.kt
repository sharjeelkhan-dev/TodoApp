package com.todoapp.domain.repository

import com.todoapp.domain.model.AIAction
import com.todoapp.domain.model.Task

interface AIRepository {
    suspend fun getPrioritizationScores(tasks: List<Task>): Result<Map<String, Int>>
    suspend fun processTaskCommand(prompt: String, currentTasks: List<Task>): Result<List<AIAction>>
}
