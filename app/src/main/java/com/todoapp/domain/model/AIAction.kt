package com.todoapp.domain.model

import java.util.Date

sealed class AIAction {

    // Naya task add karne ke liye (Sath me sub-tasks list bhi pass kar sakte hain)
    data class Add(
        val title: String,
        val description: String = "",
        val priority: TaskPriority = TaskPriority.MEDIUM,
        val category: TaskCategory = TaskCategory.OTHER,
        val dueDate: Date? = null,
        val subTasks: List<String> = emptyList()
    ) : AIAction()

    // Pehle se bane task ke under naya sub-task add karne ke liye
    data class AddSubTask(
        val taskId: String,
        val subTaskTitle: String
    ) : AIAction()

    // Existing task update karne ke liye
    data class Update(
        val taskId: String,
        val title: String? = null,
        val description: String? = null,
        val priority: TaskPriority? = null,
        val category: TaskCategory? = null,
        val dueDate: Date? = null
    ) : AIAction()

    // Single task delete karne ke liye
    data class Delete(val taskId: String) : AIAction()

    // Multiple tasks ek sath delete karne ke liye (e.g., "Delete all completed tasks")
    data class DeleteMultiple(val taskIds: List<String>) : AIAction()

    // Sub-task delete karne ke liye
    data class DeleteSubTask(
        val taskId: String,
        val subTaskId: String
    ) : AIAction()

    // Main task complete / incomplete toggle karne ke liye
    data class ToggleCompletion(
        val taskId: String,
        val isCompleted: Boolean
    ) : AIAction()

    // Sub-task complete / incomplete toggle karne ke liye
    data class ToggleSubTaskCompletion(
        val taskId: String,
        val subTaskId: String,
        val isCompleted: Boolean
    ) : AIAction()

    // Task ke andar breakdown sub-tasks auto-generate karne ke liye
    data class GenerateSubTasks(
        val taskId: String
    ) : AIAction()
}