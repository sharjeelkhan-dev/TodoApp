package com.todoapp.domain.model

/**
 * Configuration for filtering tasks in the list view.
 *
 * @param status null = all, true = completed, false = pending
 * @param category null = all categories
 * @param priority null = all priorities
 * @param sortOrder how to sort the results
 */
data class FilterOption(
    val status: Boolean? = null,
    val category: TaskCategory? = null,
    val priority: TaskPriority? = null,
    val sortOrder: SortOrder = SortOrder.DATE_CREATED_DESC
)
