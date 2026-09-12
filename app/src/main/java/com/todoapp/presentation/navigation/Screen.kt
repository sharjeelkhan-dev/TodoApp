package com.todoapp.presentation.navigation


sealed class Screen(val route: String) {
    data object Splash : Screen("splash_screen")
    data object Login : Screen("login_screen")
    data object Main : Screen("main_screen")
    data object TaskList : Screen("task_list_screen")
    data object Focus : Screen("focus_screen")
    data object AddEditTask : Screen("add_edit_task_screen/{taskId}") {
        fun createRoute(taskId: String? = null) = "add_edit_task_screen/${taskId ?: "new"}"
    }
    data object Settings : Screen("settings_screen")
}
