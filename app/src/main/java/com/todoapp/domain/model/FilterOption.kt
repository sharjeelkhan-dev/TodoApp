package com.todoapp.domain.model

data class FilterOption(
    val status: Boolean? = null,
    val category: TaskCategory? = null,
    val priority: TaskPriority? = null,
    val sortOrder: SortOrder = SortOrder.DATE_CREATED_DESC
)
