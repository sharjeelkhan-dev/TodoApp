package com.todoapp.presentation.screens.tasklist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todoapp.domain.model.SortOrder
import com.todoapp.domain.model.TaskCategory
import com.todoapp.domain.model.TaskPriority
import com.todoapp.presentation.screens.tasklist.components.TaskCard

/**
 * Main task list screen.
 * Features: Large top app bar, search, filter bottom sheet,
 * swipe-to-dismiss task cards, FAB, and undo snackbar.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskListScreen(
    state: TaskListState,
    onEvent: (TaskListEvent) -> Unit,
    onNavigateToAddTask: () -> Unit,
    onNavigateToEditTask: (String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val bottomSheetState = rememberModalBottomSheetState()

    // Show undo snackbar when a task is deleted
    LaunchedEffect(state.recentlyDeletedTask) {
        state.recentlyDeletedTask?.let { task ->
            val result = snackbarHostState.showSnackbar(
                message = "\"${task.title}\" deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) {
                onEvent(TaskListEvent.UndoDelete)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = {
                    if (state.isSearchActive) {
                        Box(
                            modifier = Modifier.offset(x = 1.7.dp,),
                            contentAlignment = Alignment.Center
                        ) {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = { onEvent(TaskListEvent.SearchQueryChanged(it)) },
                                placeholder = { Text("Search tasks…") },
                                modifier = Modifier
                                    .fillMaxWidth(0.95f),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            )
                        }
                    } else {
                        Column {
                            Text(modifier = Modifier.padding(14.dp),
                                text = "My Tasks",
                                fontWeight = FontWeight.Bold,
                            )
                            Text(modifier = Modifier.offset(x = 17.dp, y = (-5).dp),
                                text = "${state.tasks.count { !it.isCompleted }} pending · ${state.tasks.count { it.isCompleted }} done",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Search toggle
                    IconButton(onClick = { onEvent(TaskListEvent.ToggleSearch) }) {
                        Icon(
                            imageVector = if (state.isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    }
                    // Filter
                    IconButton(onClick = { onEvent(TaskListEvent.ToggleFilterSheet) }) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = "Filter"
                        )
                    }
                    // Settings
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Loading indicator
            AnimatedVisibility(visible = state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (state.tasks.isEmpty() && !state.isLoading) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inbox,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No tasks yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        Text(
                            text = "Tap + to add your first task",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Task list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.tasks,
                        key = { it.id }
                    ) { task ->
                        TaskCard(
                            task = task,
                            onToggleCompletion = {
                                onEvent(TaskListEvent.ToggleCompletion(task.id))
                            },
                            onDelete = {
                                onEvent(TaskListEvent.DeleteTask(task))
                            },
                            onClick = { onNavigateToEditTask(task.id) },
                            modifier = Modifier.animateItem()
                        )
                    }

                    // Bottom spacing for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // ─── Filter Bottom Sheet ──────────────────
    if (state.showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(TaskListEvent.ToggleFilterSheet) },
            sheetState = bottomSheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Filter & Sort",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Status filter
                Text("Status", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.filter.status == null,
                        onClick = { onEvent(TaskListEvent.FilterByStatus(null)) },
                        label = { Text("All") }
                    )
                    FilterChip(
                        selected = state.filter.status == false,
                        onClick = { onEvent(TaskListEvent.FilterByStatus(false)) },
                        label = { Text("Pending") }
                    )
                    FilterChip(
                        selected = state.filter.status == true,
                        onClick = { onEvent(TaskListEvent.FilterByStatus(true)) },
                        label = { Text("Completed") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category filter
                Text("Category", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.filter.category == null,
                        onClick = { onEvent(TaskListEvent.FilterByCategory(null)) },
                        label = { Text("All") }
                    )
                    TaskCategory.entries.forEach { category ->
                        FilterChip(
                            selected = state.filter.category == category,
                            onClick = { onEvent(TaskListEvent.FilterByCategory(category)) },
                            label = { Text(category.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority filter
                Text("Priority", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = state.filter.priority == null,
                        onClick = { onEvent(TaskListEvent.FilterByPriority(null)) },
                        label = { Text("All") }
                    )
                    TaskPriority.entries.forEach { priority ->
                        FilterChip(
                            selected = state.filter.priority == priority,
                            onClick = { onEvent(TaskListEvent.FilterByPriority(priority)) },
                            label = { Text(priority.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sort order
                Text("Sort By", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortOrder.entries.forEach { sortOrder ->
                        FilterChip(
                            selected = state.filter.sortOrder == sortOrder,
                            onClick = { onEvent(TaskListEvent.SortBy(sortOrder)) },
                            label = { Text(sortOrder.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
