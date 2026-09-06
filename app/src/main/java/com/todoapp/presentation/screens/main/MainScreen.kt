package com.todoapp.presentation.screens.main
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.todoapp.R
import com.todoapp.domain.model.SubTask
import com.todoapp.domain.model.Task
import com.todoapp.presentation.navigation.Screen
import com.todoapp.presentation.screens.focus.FocusScreen
import com.todoapp.presentation.screens.tasklist.TaskListEvent
import com.todoapp.presentation.screens.tasklist.TaskListScreen
import com.todoapp.presentation.screens.tasklist.TaskListState
import com.todoapp.presentation.screens.tasklist.TaskListViewModel
import com.todoapp.presentation.theme.TodoAppTheme

@Composable
fun MainScreen(
    navController: NavController,
    isDarkMode: Boolean,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val viewModel: TaskListViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.onEvent(TaskListEvent.ClearDeletedTask)
        }
    }

    MainScreenContent(
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        state = state,
        isDarkMode = isDarkMode,
        onEvent = viewModel::onEvent,
        onNavigateToAddEditTask = { taskId ->
            navController.navigate(Screen.AddEditTask.createRoute(taskId))
        },
        onNavigateToSettings = {
            navController.navigate(Screen.Settings.route)
        }
    )
}

@Composable
fun MainScreenContent(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    state: TaskListState,
    isDarkMode: Boolean,
    onEvent: (TaskListEvent) -> Unit,
    onNavigateToAddEditTask: (String?) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val appBg = if (isDarkMode) Color(0xFF121212) else Color(0xFFFBFBF9)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = appBg,
        bottomBar = {
            TodoBottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                isDarkMode = isDarkMode
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> {
                    MainContent(
                        state = state,
                        isDarkMode = isDarkMode,
                        onEvent = onEvent,
                        onNavigateToAddEditTask = onNavigateToAddEditTask,
                        onNavigateToSettings = onNavigateToSettings,
                        bottomPadding = paddingValues.calculateBottomPadding()
                    )
                }
                1 -> {
                    FocusScreen(isDarkMode = isDarkMode)
                }
            }
        }
    }
}

@Composable
fun TodoBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDarkMode: Boolean
) {
    val brandColor = Color(0xFF7B61FF)
    NavigationBar(
        containerColor = if (isDarkMode) Color(0xFF1E293B) else Color.White,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = stringResource(R.string.tasks_label)
                )
            },
            label = { Text(stringResource(R.string.tasks_label)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = brandColor,
                selectedTextColor = brandColor,
                indicatorColor = brandColor.copy(alpha = 0.1f)
            )
        )
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.challenge_icon),
                    contentDescription = stringResource(R.string.focus),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text(stringResource(R.string.focus)) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = brandColor,
                selectedTextColor = brandColor,
                indicatorColor = brandColor.copy(alpha = 0.1f)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(
    state: TaskListState,
    isDarkMode: Boolean,
    onEvent: (TaskListEvent) -> Unit,
    onNavigateToAddEditTask: (String?) -> Unit,
    onNavigateToSettings: () -> Unit,
    initiallyExpanded: Boolean = false,
    bottomPadding: androidx.compose.ui.unit.Dp = 0.dp
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val brandColor = Color(0xFF7B61FF)

    Box(modifier = Modifier.fillMaxSize()) {
        TaskListScreen(
            state = state,
            isDarkMode = isDarkMode,
            onEvent = onEvent,
            onNavigateToEditTask = { taskId -> onNavigateToAddEditTask(taskId) },
            onNavigateToSettings = onNavigateToSettings,
            snackbarHostState = snackbarHostState,
            contentPadding = PaddingValues(bottom = bottomPadding),
            initiallyExpanded = initiallyExpanded
        )

        FloatingActionButton(
            onClick = { onNavigateToAddEditTask(null) },
            containerColor = brandColor,
            contentColor = Color.White,
            shape = androidx.compose.foundation.shape.CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = bottomPadding + 16.dp, end = 16.dp) // Offset by bottom bar height
                .size(60.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.plus_line_icon),
                contentDescription = stringResource(R.string.add_task),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
// ─── Previews ──────────────────────────────────────────────────

@Preview(showBackground = true, name = "Bottom Bar - Light")
@Composable
fun TodoBottomBarPreviewLight() {
    TodoAppTheme(darkTheme = false) {
        TodoBottomBar(
            selectedTab = 0,
            onTabSelected = {},
            isDarkMode = false
        )
    }
}

@Preview(showBackground = true, name = "Bottom Bar - Dark", backgroundColor = 0xFF121212)
@Composable
fun TodoBottomBarPreviewDark() {
    TodoAppTheme(darkTheme = true) {
        TodoBottomBar(
            selectedTab = 1,
            onTabSelected = {},
            isDarkMode = true
        )
    }
}

@Preview(showBackground = true, name = "Full Screen - Light")
@Composable
fun MainScreenContentPreviewLight() {
    val mockTasks = listOf(
        Task(id = "1", title = "Industry Standard UI", category = com.todoapp.domain.model.TaskCategory.WORK, priority = com.todoapp.domain.model.TaskPriority.HIGH),
        Task(id = "2", title = "Premium Edge-to-Edge", subTasks = listOf(SubTask(title = "Fix Insets")), category = com.todoapp.domain.model.TaskCategory.STUDY)
    )
    TodoAppTheme(darkTheme = false) {
        MainScreenContent(
            selectedTab = 0,
            onTabSelected = {},
            state = TaskListState(tasks = mockTasks),
            isDarkMode = false,
            onEvent = {},
            onNavigateToAddEditTask = {},
            onNavigateToSettings = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty State - Light")
@Composable
fun MainContentEmptyPreviewLight() {
    TodoAppTheme(darkTheme = false) {
        MainContent(
            state = TaskListState(),
            isDarkMode = false,
            onEvent = {},
            onNavigateToAddEditTask = {},
            onNavigateToSettings = {}
        )
    }
}

@Preview(showBackground = true, name = "Empty State - Dark", backgroundColor = 0xFF121212)
@Composable
fun MainContentEmptyPreviewDark() {
    TodoAppTheme(darkTheme = true) {
        MainContent(
            state = TaskListState(),
            isDarkMode = true,
            onEvent = {},
            onNavigateToAddEditTask = {},
            onNavigateToSettings = {}
        )
    }
}

@Preview(showBackground = true, name = "With Tasks - Light")
@Composable
fun MainContentWithTasksPreviewLight() {
    val mockTasks = listOf(
        Task(id = "1", title = "Industry Standard UI", category = com.todoapp.domain.model.TaskCategory.WORK, priority = com.todoapp.domain.model.TaskPriority.HIGH),
        Task(id = "2", title = "Premium Edge-to-Edge", subTasks = listOf(SubTask(title = "Fix Insets"), SubTask(title = "Add Padding")), category = com.todoapp.domain.model.TaskCategory.STUDY),
        Task(id = "3", title = "Completed Task", isCompleted = true, category = com.todoapp.domain.model.TaskCategory.PERSONAL)
    )
    TodoAppTheme(darkTheme = false) {
        MainContent(
            state = TaskListState(tasks = mockTasks),
            isDarkMode = false,
            onEvent = {},
            onNavigateToAddEditTask = {},
            onNavigateToSettings = {},
            initiallyExpanded = true
        )
    }
}

@Preview(showBackground = true, name = "With Tasks - Dark", backgroundColor = 0xFF121212)
@Composable
fun MainContentWithTasksPreviewDark() {
    val mockTasks = listOf(
        Task(id = "1", title = "Industry Standard UI", category = com.todoapp.domain.model.TaskCategory.WORK, priority = com.todoapp.domain.model.TaskPriority.HIGH),
        Task(id = "2", title = "Premium Edge-to-Edge", subTasks = listOf(SubTask(title = "Fix Insets")), category = com.todoapp.domain.model.TaskCategory.STUDY)
    )
    TodoAppTheme(darkTheme = true) {
        MainContent(
            state = TaskListState(tasks = mockTasks),
            isDarkMode = true,
            onEvent = {},
            onNavigateToAddEditTask = {},
            onNavigateToSettings = {},
            initiallyExpanded = true
        )
    }
}

@Preview(showBackground = true, name = "AI Thinking State")
@Composable
fun MainContentAIThinkingPreview() {
    TodoAppTheme {
        MainContent(
            state = TaskListState(isAIThinking = true),
            isDarkMode = false,
            onEvent = {},
            onNavigateToAddEditTask = {},
            onNavigateToSettings = {}
        )
    }
}

@Preview(showBackground = true, name = "Search Active State")
@Composable
fun MainContentSearchActivePreview() {
    TodoAppTheme {
        MainContent(
            state = TaskListState(isSearchActive = true, searchQuery = "Premium"),
            isDarkMode = false,
            onEvent = {},
            onNavigateToAddEditTask = {},
            onNavigateToSettings = {}
        )
    }
}