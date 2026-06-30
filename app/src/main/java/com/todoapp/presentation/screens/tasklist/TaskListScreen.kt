package com.todoapp.presentation.screens.tasklist
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import com.todoapp.presentation.theme.PoppinsFontFamily
import com.todoapp.presentation.theme.TodoAppTheme
import androidx.compose.ui.text.TextStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds

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
    val brandColor = Color(0xFF7B61FF)
    val successColor = Color(0xFF22C55E)
    val secondaryText = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF8E8E93)
    val primaryText = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val cardBg = if (isDarkMode) Color(0xFF231F26) else Color.White
    val bgColor = if (isDarkMode) Color(0xFF1C1B21) else Color(0xFFFBFBF9)
    
    val searchFocusRequester = remember { FocusRequester() }

    val activeTasks = remember(state.tasks) { state.tasks.filter { !it.isCompleted } }
    val completedTasks = remember(state.tasks) { state.tasks.filter { it.isCompleted } }
    val isListEmpty = state.tasks.isEmpty() && !state.isLoading

    val taskDeletedMessage = stringResource(R.string.task_deleted)
    val undoLabel = stringResource(R.string.undo)

    LaunchedEffect(state.recentlyDeletedTask) {
        val task = state.recentlyDeletedTask ?: return@LaunchedEffect
        try {
            val result = snackbarHostState.showSnackbar(
                message = "\"${task.title}\" $taskDeletedMessage",
                actionLabel = undoLabel,
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

    LaunchedEffect(state.isSearchActive) {
        if (state.isSearchActive) {
            kotlinx.coroutines.delay(100.milliseconds)
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
                    placeholder = { Text(stringResource(R.string.search_tasks_hint)) },
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
                                contentDescription = stringResource(R.string.close),
                                tint = secondaryText)
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { onEvent(TaskListEvent.ToggleSearch) }
                    ),
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
                    modifier = Modifier.padding(16.dp),
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
                        text = stringResource(R.string.gemini_working),
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
        val suggestions = listOf("Add task...", "Mark as done...", "Delete old...")

        Dialog(
            onDismissRequest = { onEvent(TaskListEvent.ToggleAICommandDialog) },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = cardBg,
                tonalElevation = 8.dp,
                border = BorderStroke(1.dp, brandColor.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(brandColor.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = brandColor, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = stringResource(R.string.ai_task_assistant),
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = primaryText
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onEvent(TaskListEvent.ToggleAICommandDialog) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Close, null, tint = secondaryText, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Area
                    OutlinedTextField(
                        value = aiPrompt,
                        onValueChange = { aiPrompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { 
                            Text(
                                stringResource(R.string.how_can_i_help),
                                style = TextStyle(fontFamily = PoppinsFontFamily, fontSize = 14.sp)
                            ) 
                        },
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 3,
                        textStyle = TextStyle(fontFamily = PoppinsFontFamily, color = primaryText, fontSize = 15.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = brandColor,
                            unfocusedBorderColor = secondaryText.copy(alpha = 0.2f),
                            focusedContainerColor = bgColor.copy(alpha = 0.5f),
                            unfocusedContainerColor = bgColor.copy(alpha = 0.5f),
                            cursorColor = brandColor
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Suggestions (Compact Row)
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestions) { suggestion ->
                            SuggestionChip(
                                onClick = { aiPrompt = suggestion },
                                label = { 
                                    Text(
                                        suggestion, 
                                        style = TextStyle(fontFamily = PoppinsFontFamily, fontSize = 12.sp)
                                    ) 
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    labelColor = brandColor,
                                    containerColor = brandColor.copy(alpha = 0.05f)
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = brandColor.copy(alpha = 0.1f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { onEvent(TaskListEvent.SmartPrioritize) },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Icon(painterResource(R.drawable.star), null, tint = brandColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.auto_prioritize),
                                style = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold, color = brandColor)
                            )
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = { 
                                if (aiPrompt.isNotBlank()) {
                                    onEvent(TaskListEvent.ExecuteAICommand(aiPrompt))
                                }
                            },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = brandColor),
                            enabled = aiPrompt.isNotBlank()
                        ) {
                            Text(
                                stringResource(R.string.execute),
                                style = TextStyle(fontFamily = PoppinsFontFamily, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor),
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

        if (state.searchQuery.isNotEmpty()) {
            item {
                Surface(
                    onClick = { onEvent(TaskListEvent.SearchQueryChanged("")) },
                    shape = RoundedCornerShape(12.dp),
                    color = brandColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, brandColor.copy(alpha = 0.2f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = brandColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${stringResource(R.string.results_for)} \"${state.searchQuery}\"",
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                color = brandColor
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = brandColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
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
                        text = stringResource(R.string.no_tasks_found),
                        color = secondaryText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            if (activeTasks.isNotEmpty()) {
                item { SectionHeader(stringResource(R.string.active), secondaryText) }
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
                item { SectionHeader(stringResource(R.string.completed), secondaryText) }
                items(completedTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        isDarkMode = isDarkMode,
                        modifier = Modifier.offset(y = (-10).dp),
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
                text = stringResource(R.string.my_tasks),
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
    val stats = remember(tasks) {
        val total = tasks.size + tasks.sumOf { it.subTasks.size }
        val done = tasks.count { it.isCompleted } + tasks.sumOf { it.subTasks.count { st -> st.isCompleted } }
        total to done
    }
    val totalWorkItems = stats.first
    val doneWorkItems = stats.second
    val pendingWorkItems = totalWorkItems - doneWorkItems
    
    val progress = if (totalWorkItems == 0) 0f else doneWorkItems.toFloat() / totalWorkItems
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    val pendingSuffix = stringResource(R.string.pending).lowercase()
    val doneSuffix = stringResource(R.string.completed).lowercase()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Stats Row
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatChip(text = "$pendingWorkItems $pendingSuffix", color = brandColor, isDarkMode = isDarkMode)
            StatChip(text = "$doneWorkItems $doneSuffix", color = successColor, isDarkMode = isDarkMode)
        }

        // Progress Section
        Column(modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.todays_progress),
                color = secondaryText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            
            Box(contentAlignment = Alignment.CenterStart) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.fillMaxWidth()
                        .height(6.dp)
                        .offset(y = (-5).dp)
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
                        .offset(y = (-5).dp)
                        .clip(CircleShape)
                        .background(successColor)
                )
            }
            Text(
                text = "${(progress * 100).toInt()}%",
                color = successColor,
                modifier = Modifier.offset(y = (-10).dp),
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
private fun StatChip(text: String, color: Color, isDarkMode: Boolean)
{
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isDarkMode) Color(0xFF1E1E1E) else Color.White,
        border = BorderStroke(1.dp, if (isDarkMode)
            Color(0xFF2C2C2C) else Color(0xFFF1F5F9)),
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape)
                .background(color))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, color = if (isDarkMode) Color(0xFF94A3B8)
            else Color(0xFF475569),
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp)
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
            .padding(top = 5.dp,
                bottom = 4.dp))
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