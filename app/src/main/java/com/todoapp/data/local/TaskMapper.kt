package com.todoapp.data.local

import com.todoapp.data.local.entity.TaskEntity
import com.todoapp.domain.model.Task
import com.todoapp.domain.model.TaskCategory
import com.todoapp.domain.model.TaskPriority
import java.util.Date

/**
 * Mapper extension functions to convert between TaskEntity (data layer)
 * and Task (domain layer). Keeps domain models clean of Room annotations.
 */

/** Convert Room entity to domain model. */
fun TaskEntity.toDomainModel(): Task {
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
        dueDate = dueDate?.let { Date(it) },
        dueTime = dueTime,
        createdAt = Date(createdAt),
        updatedAt = Date(updatedAt),
        userId = userId,
        isSynced = syncStatus == 0
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
        dueDate = dueDate?.time,
        dueTime = dueTime,
        createdAt = createdAt.time,
        updatedAt = updatedAt.time,
        userId = userId,
        syncStatus = syncStatus
    )
}

/** Convert a list of entities to domain models. */
fun List<TaskEntity>.toDomainModels(): List<Task> = map { it.toDomainModel() }
