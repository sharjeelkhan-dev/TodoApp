package com.todoapp.presentation.screens.tasklist
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.todoapp.R
import com.todoapp.domain.model.SubTask
import com.todoapp.domain.model.Task
import com.todoapp.domain.model.TaskPriority
import com.todoapp.presentation.screens.tasklist.components.SortBottomSheet
import com.todoapp.presentation.screens.tasklist.components.TaskCard
import com.todoapp.presentation.theme.TodoAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TaskListScreen(
    state: TaskListState,
    isDarkMode: Boolean,
    onEvent: (TaskListEvent) -> Unit,
    onNavigateToEditTask: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    snackbarHostState: SnackbarHostState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    initiallyExpanded: Boolean = false,
) {
    val listState = rememberLazyListState()
    val isDarkTheme = isSystemInDarkTheme()
    val scrollbarBaseColor = if (isDarkTheme) Color.White else Color.Black
    val brandColor = Color(0xFF7B61FF)
    val successColor = Color(0xFF22C55E)
    val secondaryText = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF8E8E93)
    val primaryText = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val cardBg = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val bgColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFFBFBF9)
    
    val searchFocusRequester = remember { FocusRequester() }

    val isListEmpty = state.tasks.isEmpty() && !state.isLoading

    LaunchedEffect(state.recentlyDeletedTask) {
        val task = state.recentlyDeletedTask ?: return@LaunchedEffect
        try {
            val result = snackbarHostState.showSnackbar(
                message = "\"${task.title}\" deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Long
            )
            if (result == SnackbarResult.ActionPerformed) onEvent(TaskListEvent.UndoDelete)
        } finally {
            onEvent(TaskListEvent.ClearDeletedTask)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
            onEvent(TaskListEvent.ClearError)
        }
    }

    // search field when search mode is activated
    LaunchedEffect(state.isSearchActive) {
        if (state.isSearchActive) {
            // Small delay to ensure dialog is composed before requesting focus
            kotlinx.coroutines.delay(100)
            searchFocusRequester.requestFocus()
        }
    }

    if (state.showFilterSheet) {
        SortBottomSheet(
            selectedSortOrder = state.filter.sortOrder,
            onSortOrderSelected = { order ->
                onEvent(TaskListEvent.SortBy(order))
            },
            onDismiss = { onEvent(TaskListEvent.ToggleFilterSheet) },
            onReset = { onEvent(TaskListEvent.SortBy(com.todoapp.domain.model.SortOrder.DATE_CREATED_DESC)) }
        )
    }

    // --- Search Pop-up Dialog ---
    if (state.isSearchActive) {
        Dialog(
            onDismissRequest = { onEvent(TaskListEvent.ToggleSearch) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { onEvent(TaskListEvent.ToggleSearch) }
                    .safeDrawingPadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { onEvent(TaskListEvent.SearchQueryChanged(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                        .focusRequester(searchFocusRequester)
                        .clickable(enabled = false) { }, // Prevent dismissing when clicking the bar itself
                    placeholder = { Text("Search tasks...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = brandColor) },
                    trailingIcon = {
                        IconButton(onClick = { 
                            if (state.searchQuery.isNotEmpty()) {
                                onEvent(TaskListEvent.SearchQueryChanged(""))
                            } else {
                                onEvent(TaskListEvent.ToggleSearch)
                            }
                        }) {
                            Icon(Icons.Default.Close,
                                contentDescription = "Close",
                                tint = secondaryText)
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = brandColor,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText
                    )
                )
            }
        }
    }

    if (state.isAIThinking) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Surface(
                modifier = Modifier.size(180.dp),
                shape = RoundedCornerShape(32.dp),
                color = cardBg,
                tonalElevation = 12.dp,
                border = BorderStroke(1.dp, brandColor.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = brandColor,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Gemini is working...",
                        style = MaterialTheme.typography.labelLarge,
                        color = brandColor,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    if (state.isAICommandDialogOpen) {
        var aiPrompt by remember { mutableStateOf("") }
        val suggestions = listOf(
            "Add task buy groceries",
            "Mark study as done",
            "Delete old tasks"
        )

        Dialog(
            onDismissRequest = { onEvent(TaskListEvent.ToggleAICommandDialog) }
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = cardBg,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, brandColor.copy(alpha = 0.2f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(brandColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = brandColor,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "AI Task Assistant",
                        style = MaterialTheme.typography.headlineSmall,
                        color = primaryText,
                        fontWeight = FontWeight.ExtraBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Tell me what you'd like to do with your tasks.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = secondaryText,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))

                    // Suggestion Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        suggestions.forEach { suggestion ->
                            SuggestionChip(
                                onClick = { aiPrompt = suggestion },
                                label = { 
                                    Text(
                                        suggestion.take(15) + "...", 
                                        style = MaterialTheme.typography.labelSmall
                                    ) 
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Type here...") },
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brandColor,
                            unfocusedBorderColor = secondaryText.copy(alpha = 0.3f),
                            focusedContainerColor = bgColor.copy(alpha = 0.5f),
                            unfocusedContainerColor = bgColor.copy(alpha = 0.5f),
                            focusedTextColor = primaryText,
                            unfocusedTextColor = primaryText
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onEvent(TaskListEvent.ToggleAICommandDialog) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, secondaryText.copy(alpha = 0.3f))
                        ) {
                            Text("Cancel", color = secondaryText)
                        }
                        
                        Button(
                            onClick = { 
                                if (aiPrompt.isNotBlank()) {
                                    onEvent(TaskListEvent.ExecuteAICommand(aiPrompt))
                                }
                            },
                            modifier = Modifier.weight(1.5f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                            enabled = aiPrompt.isNotBlank()
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Execute")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TextButton(
                        onClick = { onEvent(TaskListEvent.SmartPrioritize) }
                    ) {
                        Text("✨ Auto-Prioritize All Tasks", color = brandColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScrollbar(
                state = listState,
                color = scrollbarBaseColor.copy(alpha = 0.3f)
            ),
        state = listState,
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 16.dp,
            bottom = contentPadding.calculateBottomPadding() + 100.dp,
            start = 24.dp,
            end = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        // --- Header Section ---
        item {
            HeaderTopRow(
                secondaryText = secondaryText,
                primaryText = primaryText,
                brandColor = brandColor,
                isDarkMode = isDarkMode,
                cardBg = cardBg,
                onEvent = onEvent,
                onNavigateToSettings = onNavigateToSettings
            )
        }

        item {
            HeaderStatsAndProgress(
                tasks = state.tasks,
                brandColor = brandColor,
                successColor = successColor,
                secondaryText = secondaryText,
                isDarkMode = isDarkMode
            )
        }

        if (isListEmpty) {
            item {
                Column(
                    modifier = Modifier
                        .fillParentMaxHeight(0.6f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.no_task_icon),
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = brandColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No tasks found",
                        color = secondaryText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            val activeTasks = state.tasks.filter { !it.isCompleted }
            val completedTasks = state.tasks.filter { it.isCompleted }

            if (activeTasks.isNotEmpty()) {
                item { SectionHeader("ACTIVE", secondaryText) }
                items(activeTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        modifier = Modifier.offset(y = (-10).dp),
                        isDarkMode = isDarkMode,
                        onToggleCompletion = { onEvent(TaskListEvent.ToggleCompletion(task.id)) },
                        onToggleSubTask = { subTaskId -> onEvent(TaskListEvent.ToggleSubTask(task.id, subTaskId)) },
                        onDelete = { onEvent(TaskListEvent.DeleteTask(task)) },
                        onClick = { onNavigateToEditTask(task.id) },
                        initiallyExpanded = initiallyExpanded
                    )
                }
            }

            if (completedTasks.isNotEmpty()) {
                item { Spacer(modifier = Modifier.height(12.dp)) }
                item { SectionHeader("COMPLETED", secondaryText) }
                items(completedTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        modifier = Modifier.offset(y = (-10).dp),
                        isDarkMode = isDarkMode,
                        onToggleCompletion = { onEvent(TaskListEvent.ToggleCompletion(task.id)) },
                        onToggleSubTask = { subTaskId -> onEvent(TaskListEvent.ToggleSubTask(task.id, subTaskId)) },
                        onDelete = { onEvent(TaskListEvent.DeleteTask(task)) },
                        onClick = { onNavigateToEditTask(task.id) },
                        initiallyExpanded = initiallyExpanded
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderTopRow(
    secondaryText: Color,
    primaryText: Color,
    brandColor: Color,
    isDarkMode: Boolean,
    cardBg: Color,
    onEvent: (TaskListEvent) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEEE, d MMMM",
        Locale.getDefault()) }
    val today = dateFormat.format(Date())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = today.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = secondaryText,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            )
            Text(
                text = "My Tasks",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = primaryText,
                    fontSize = 32.sp,
                    letterSpacing = (-1).sp
                )
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
            ActionButton(
                imageVector = Icons.Default.AutoAwesome,
                isDarkMode = isDarkMode,
                cardBg = cardBg,
                tint = brandColor,
                onClick = { onEvent(TaskListEvent.ToggleAICommandDialog) }
            )
            ActionButton(
                iconRes = R.drawable.magnifier_glass_icon,
                isDarkMode = isDarkMode,
                cardBg = cardBg,
                tint = secondaryText,
                onClick = { onEvent(TaskListEvent.ToggleSearch) }
            )
            ActionButton(
                iconRes = R.drawable.filter,
                isDarkMode = isDarkMode,
                cardBg = cardBg,
                tint = secondaryText,
                onClick = { onEvent(TaskListEvent.ToggleFilterSheet) }
            )
            ActionButton(
                iconRes = R.drawable.settings_1_fill,
                isDarkMode = isDarkMode,
                cardBg = cardBg,
                tint = secondaryText,
                onClick = onNavigateToSettings
            )
        }
    }
}

@Composable
private fun HeaderStatsAndProgress(
    tasks: List<Task>,
    brandColor: Color,
    successColor: Color,
    secondaryText: Color,
    isDarkMode: Boolean
) {
    val totalWorkItems = tasks.size + tasks.sumOf { it.subTasks.size }
    val doneWorkItems = tasks.count { it.isCompleted } + tasks.sumOf { it.subTasks.count { st -> st.isCompleted } }
    val pendingWorkItems = totalWorkItems - doneWorkItems
    
    val progress = if (totalWorkItems == 0) 0f else doneWorkItems.toFloat() / totalWorkItems
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Stats Row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatChip(text = "$pendingWorkItems pending", color = brandColor, isDarkMode = isDarkMode)
            StatChip(text = "$doneWorkItems done", color = successColor, isDarkMode = isDarkMode)
        }

        // Progress Section
        Column(modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Today's progress",
                color = secondaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            
            Box(contentAlignment = Alignment.CenterStart) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = successColor,
                    trackColor = if (isDarkMode)
                        Color(0xFF2C2C2C)
                    else Color(0xFFE2E8F0),
                    strokeCap = StrokeCap.Round,
                    drawStopIndicator = {},
                    gapSize = 0.dp,
                )
                // Green start indicator dot
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(successColor)
                )
            }

            Text(
                text = "${(progress * 100).toInt()}%",
                color = successColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ActionButton(
    iconRes: Int? = null,
    imageVector: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isDarkMode: Boolean,
    cardBg: Color,
    tint: Color,
    onClick: () -> Unit
) {
    val dividerColor = if (isDarkMode)
        Color(0xFF2C2C2C)
    else Color(0xFFEEEEEE)
    Surface(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = cardBg,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, dividerColor)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (iconRes != null) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = tint
                )
            } else if (imageVector != null) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = tint
                )
            }
        }
    }
}

@Composable
private fun StatChip(text: String, color: Color, isDarkMode: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isDarkMode) Color(0xFF1E1E1E) else Color.White,
        border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFF1F5F9)),
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF475569), fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SectionHeader(title: String, color: Color) {
    Text(text = title, color = color.copy(alpha = 0.7f),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier
            .padding(top = 16.dp,
                bottom = 4.dp))
}

fun Modifier.verticalScrollbar(
    state: androidx.compose.foundation.lazy.LazyListState,
    width: Dp = 4.dp,
    color: Color = Color.Gray
): Modifier = drawWithContent {
    drawContent()
    val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
    val totalElementCount = state.layoutInfo.totalItemsCount
    if (firstVisibleElementIndex != null && totalElementCount > 0) {
        val elementHeight = size.height / totalElementCount
        val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight
        val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width - width.toPx(), scrollbarOffsetY),
            size = Size(width.toPx(), scrollbarHeight),
            cornerRadius = CornerRadius(width.toPx() / 2, width.toPx() / 2)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskListScreenPreview() {
    TodoAppTheme {
        TaskListScreen(
            state = TaskListState(
                tasks = listOf(
                    Task(
                        id = "1", 
                        title = "Complete Project Proposal", 
                        description = "Finish the draft and send for review", 
                        priority = TaskPriority.HIGH,
                        subTasks = listOf(
                            SubTask(title = "Research", isCompleted = true),
                            SubTask(title = "Drafting", isCompleted = false),
                            SubTask(title = "Review", isCompleted = false)
                        )
                    ),
                    Task(id = "2", title = "Buy Groceries", description = "Milk, Eggs, Bread, Fruits", priority = TaskPriority.MEDIUM),
                    Task(id = "3", title = "Morning Run", description = "5km around the park", isCompleted = true, priority = TaskPriority.LOW)
                )
            ),
            isDarkMode = false,
            onEvent = {},
            onNavigateToEditTask = {},
            onNavigateToSettings = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
