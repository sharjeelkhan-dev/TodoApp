package com.todoapp.presentation.navigation

/**
 * Defines all navigation routes/screens in the app.
 */
sealed class Screen(val route: String) {
    data object Auth : Screen("auth_screen")
    data object TaskList : Screen("task_list_screen")
    data object AddEditTask : Screen("add_edit_task_screen/{taskId}") {
        fun createRoute(taskId: String? = null) = "add_edit_task_screen/${taskId ?: "new"}"
    }
    data object Settings : Screen("settings_screen")
}
