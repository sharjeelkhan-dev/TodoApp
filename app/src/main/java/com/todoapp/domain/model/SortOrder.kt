package com.todoapp.domain.model

/**
 * Defines how tasks can be sorted in the list view.
 */
enum class SortOrder(val label: String) {
    DATE_CREATED_ASC("Oldest First"),
    DATE_CREATED_DESC("Newest First"),
    DUE_DATE_ASC("Due Date (Earliest)"),
    DUE_DATE_DESC("Due Date (Latest)"),
    PRIORITY_HIGH_FIRST("Priority (High → Low)"),
    PRIORITY_LOW_FIRST("Priority (Low → High)"),
    ALPHABETICAL("Alphabetical (A–Z)"),
    AI_SMART("Smart Priority (AI)")
}
