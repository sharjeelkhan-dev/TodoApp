package com.todoapp.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.todoapp.data.local.entity.TaskEntity
import com.todoapp.domain.model.SubTask
import com.todoapp.domain.model.Task
import com.todoapp.domain.model.TaskCategory
import com.todoapp.domain.model.TaskPriority
import java.util.Date

private val gson = Gson()
private val subTaskType = object : TypeToken<List<SubTask>>() {}.type

/** Convert Room entity to domain model. */
fun TaskEntity.toDomainModel(): Task {
    val decodedSubTasks: List<SubTask> = try {
        if (subTasks.isNullOrBlank()) {
            emptyList()
        } else {
            gson.fromJson<List<SubTask>>(subTasks, subTaskType) ?: emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }

    return Task(
        id = id,
        title = title,
        description = description,
        category = try {
            TaskCategory.valueOf(category)
        } catch (_: Exception) {
            TaskCategory.OTHER
        },
        priority = try {
            TaskPriority.valueOf(priority)
        } catch (_: Exception) {
            TaskPriority.MEDIUM
        },
        isCompleted = isCompleted,
        isReminderEnabled = isReminderEnabled,
        dueDate = dueDate?.let { Date(it) },
        dueTime = dueTime,
        createdAt = Date(createdAt),
        updatedAt = Date(updatedAt),
        userId = userId,
        isSynced = syncStatus == 0,
        aiPriorityScore = aiPriorityScore,
        subTasks = decodedSubTasks
    )
}

/** Convert domain model to Room entity. */
fun Task.toEntity(syncStatus: Int = 1): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        category = category.name,
        priority = priority.name,
        isCompleted = isCompleted,
        isReminderEnabled = isReminderEnabled,
        dueDate = dueDate?.time,
        dueTime = dueTime,
        createdAt = createdAt.time,
        updatedAt = updatedAt.time,
        userId = userId,
        syncStatus = syncStatus,
        aiPriorityScore = aiPriorityScore,
        subTasks = gson.toJson(subTasks)
    )
}

/** Convert a list of entities to domain models. */
fun List<TaskEntity>.toDomainModels(): List<Task> = map { it.toDomainModel() }
