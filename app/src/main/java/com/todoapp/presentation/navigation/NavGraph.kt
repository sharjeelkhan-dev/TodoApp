package com.todoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.todoapp.presentation.screens.addedittask.AddEditTaskScreen
import com.todoapp.presentation.screens.addedittask.AddEditTaskViewModel
import com.todoapp.presentation.screens.auth.LoginScreen
import com.todoapp.presentation.screens.auth.LoginViewModel
import com.todoapp.presentation.screens.main.MainScreen
import com.todoapp.presentation.screens.splash.SplashScreen
import com.todoapp.presentation.screens.splash.SplashViewModel
import com.todoapp.presentation.screens.settings.SettingsScreen
import com.todoapp.presentation.screens.settings.SettingsViewModel

/**
 * Main navigation graph for the app.
 * Starts with Splash Screen which logs in user anonymously in background.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // ─── Splash Screen ──────────────────────────────
        composable(route = Screen.Splash.route) {
            val viewModel: SplashViewModel = hiltViewModel()
            val tasksCount by viewModel.tasksCount.collectAsState()
            val doneCount by viewModel.doneCount.collectAsState()
            val isAuthenticated by viewModel.isAuthenticated.collectAsState()

            SplashScreen(
                isDarkMode = isDarkMode,
                tasksCount = tasksCount,
                doneCount = doneCount,
            ) {
                val destination = if (isAuthenticated) Screen.Main.route else Screen.Login.route
                navController.navigate(destination) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        }

        // ─── Login Screen ──────────────────────────────
        composable(route = Screen.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LoginScreen(
                state = state,
                isDarkMode = isDarkMode,
                onEvent = viewModel::onEvent,
                onNavigateToHome = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ─── Main Screen (Tabs) ─────────────────────────
        composable(route = Screen.Main.route) {
            MainScreen(
                navController = navController,
                isDarkMode = isDarkMode
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
                isDarkMode = isDarkMode,
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
                    // Navigate to Login screen and clear the task list/main screen from stack
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
