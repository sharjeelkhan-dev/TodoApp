package com.todoapp.presentation.screens.main
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.todoapp.R
import com.todoapp.domain.model.SubTask
import com.todoapp.domain.model.Task
import com.todoapp.presentation.navigation.Screen
import com.todoapp.presentation.screens.tasklist.TaskListEvent
import com.todoapp.presentation.screens.tasklist.TaskListScreen
import com.todoapp.presentation.screens.tasklist.TaskListState
import com.todoapp.presentation.screens.tasklist.TaskListViewModel
import com.todoapp.presentation.theme.TodoAppTheme

@Composable
fun MainScreen(
    navController: NavController,
    isDarkMode: Boolean,
    viewModel: TaskListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onEvent(TaskListEvent.ClearDeletedTask)
        }
    }
    MainContent(
        state = state,
        isDarkMode = isDarkMode,
        onEvent = viewModel::onEvent,
        onNavigateToAddEditTask = { taskId ->
            navController.navigate(Screen
                .AddEditTask.createRoute(taskId))
        },
        onNavigateToSettings = {
            navController.navigate(Screen.Settings.route)
        }
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    state: TaskListState,
    isDarkMode: Boolean,
    onEvent: (TaskListEvent) -> Unit,
    onNavigateToAddEditTask: (String?) -> Unit,
    onNavigateToSettings: () -> Unit,
    initiallyExpanded: Boolean = false
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    val appBg = if (isDarkMode) Color(0xFF121212) else Color(0xFFFBFBF9)
    val brandColor = Color(0xFF7B61FF)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = appBg,
        snackbarHost = { 
            SnackbarHost(snackbarHostState) { data ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it != SwipeToDismissBoxValue.Settled) {
                            data.dismiss()
                        }
                        true
                    }
                )
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {},
                    content = { Snackbar(snackbarData = data) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddEditTask(null) },
                containerColor = brandColor,
                contentColor = Color.White,
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, end = 8.dp)
                    .size(60.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add_square_fill),
                    contentDescription = "Add",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        TaskListScreen(
            state = state,
            isDarkMode = isDarkMode,
            onEvent = onEvent,
            onNavigateToEditTask = { taskId -> onNavigateToAddEditTask(taskId) },
            onNavigateToSettings = onNavigateToSettings,
            snackbarHostState = snackbarHostState,
            contentPadding = paddingValues,
            initiallyExpanded = initiallyExpanded
        )
    }
}
@Preview(showBackground = true, name = "Main Screen with Sub-tasks")
@Composable
fun MainScreenSubTasksPreview() {
    TodoAppTheme {
        MainContent(
            state = TaskListState(
                tasks = listOf(
                    Task(
                        id = "1",
                        title = "Main Project Task",
                        subTasks = listOf(
                            SubTask(title = "Sub-task 1", isCompleted = true),
                            SubTask(title = "Sub-task 2", isCompleted = false)
                        )
                    ),
                    Task(id = "2", title = "Another Task", isCompleted = false)
                )
            ),
            isDarkMode = false,
            onEvent = {},
            onNavigateToAddEditTask = {},
            onNavigateToSettings = {},
            initiallyExpanded = true
        )
    }
}
