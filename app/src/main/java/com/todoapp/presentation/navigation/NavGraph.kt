package com.todoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.todoapp.presentation.screens.addedittask.AddEditTaskScreen
import com.todoapp.presentation.screens.addedittask.AddEditTaskViewModel
import com.todoapp.presentation.screens.auth.AuthScreen
import com.todoapp.presentation.screens.auth.AuthViewModel
import com.todoapp.presentation.screens.settings.SettingsScreen
import com.todoapp.presentation.screens.settings.SettingsViewModel
import com.todoapp.presentation.screens.tasklist.TaskListScreen
import com.todoapp.presentation.screens.tasklist.TaskListViewModel

/**
 * Main navigation graph for the app.
 * Determines start destination based on auth state.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    isSignedIn: Boolean,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit
) {
    val startDestination = if (isSignedIn) Screen.TaskList.route else Screen.Auth.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ─── Auth Screen ──────────────────────────────
        composable(route = Screen.Auth.route) {
            val viewModel: AuthViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            AuthScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateToTasks = {
                    navController.navigate(Screen.TaskList.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }

        // ─── Task List Screen ──────────────────────────────
        composable(route = Screen.TaskList.route) {
            val viewModel: TaskListViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            TaskListScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateToAddTask = {
                    navController.navigate(Screen.AddEditTask.createRoute())
                },
                onNavigateToEditTask = { taskId ->
                    navController.navigate(Screen.AddEditTask.createRoute(taskId))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // ─── Add/Edit Task Screen ──────────────────────────────
        composable(
            route = Screen.AddEditTask.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                    defaultValue = "new"
                }
            )
        ) {
            val viewModel: AddEditTaskViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            AddEditTaskScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // ─── Settings Screen ──────────────────────────────
        composable(route = Screen.Settings.route) {
            val viewModel: SettingsViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            SettingsScreen(
                state = state,
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                onEvent = viewModel::onEvent,
                onNavigateBack = { navController.popBackStack() },
                onSignedOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
