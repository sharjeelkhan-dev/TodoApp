package com.todoapp.domain.model

/**
 * Represents the category of a task.
 * Each category has a display label and an icon name (Material icon).
 */
enum class TaskCategory(val label: String, val iconName: String) {
    WORK("Work", "work"),
    PERSONAL("Personal", "person"),
    STUDY("Study", "school"),
    HEALTH("Health", "favorite"),
    SHOPPING("Shopping", "shopping_cart"),
    FINANCE("Finance", "attach_money"),
    HOME("Home", "home"),
    OTHER("Other", "more_horiz")
}
