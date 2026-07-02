package com.todoapp.presentation.screens.tasklist.components
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todoapp.R
import com.todoapp.domain.model.SubTask
import com.todoapp.domain.model.Task
import com.todoapp.domain.model.TaskCategory
import com.todoapp.domain.model.TaskPriority
import com.todoapp.presentation.theme.CategoryFinance
import com.todoapp.presentation.theme.CategoryHealth
import com.todoapp.presentation.theme.CategoryHome
import com.todoapp.presentation.theme.CategoryOther
import com.todoapp.presentation.theme.CategoryPersonal
import com.todoapp.presentation.theme.CategoryShopping
import com.todoapp.presentation.theme.CategoryStudy
import com.todoapp.presentation.theme.CategoryWork
import com.todoapp.presentation.theme.PriorityHigh
import com.todoapp.presentation.theme.PriorityLow
import com.todoapp.presentation.theme.PriorityMedium
import com.todoapp.presentation.theme.TodoAppTheme
import com.todoapp.presentation.theme.getCategoryColor
import com.todoapp.presentation.theme.getPriorityColor
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    task: Task,
    isDarkMode: Boolean,
    onToggleCompletion: () -> Unit,
    onToggleSubTask: (String) -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false
) {
    val categoryColor = getCategoryColor(task.category)

    // --- Premium Animations ---
    val completionProgress by animateFloatAsState(
        targetValue = if (task.isCompleted) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "completionProgress"
    )

    val cardBg = if (isDarkMode) Color(0xFF231F26) else Color(0xFFF5F5F7)
    val primaryText = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val secondaryText = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    val animatedContentColor by animateColorAsState(
        targetValue = if (task.isCompleted) secondaryText else primaryText,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "contentColor"
    )

    val checkBorderColor by animateColorAsState(
        targetValue = if (task.isCompleted) Color(0xFF22C55E) else {
            if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "checkBorder"
    )

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val dueTimeText = remember(task.dueTime) {
        if (task.dueTime != null) {
            try {
                val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = inputFormat.parse(task.dueTime)
                if (date != null) {
                    val start = timeFormat.format(date)
                    // Mock a range for design purposes if it's just a single time
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = date
                    calendar.add(java.util.Calendar.MINUTE, 30)
                    val end = timeFormat.format(calendar.time)
                    "$start - $end"
                } else task.dueTime
            } catch (e: Exception) {
                task.dueTime
            }
        } else {
            timeFormat.format(task.createdAt)
        }
    }

    var showMenu by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "arrowRotation"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (task.isCompleted) 0.5f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "cardAlpha"
    )

    Box(modifier = modifier.graphicsLayer { alpha = cardAlpha }) {
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.03f)
                        else Color.Black.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                shadowElevation = if (isDarkMode) 0.dp else 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Top Row: Category and Priority Bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Category Bar
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(categoryColor.copy(alpha = 0.8f))
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Priority Bar (Same style as category)
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(getPriorityColor(task.priority).copy(alpha = 0.8f))
                            )
                        }

                        Box {
                            IconButton(
                                onClick = { showMenu = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreHoriz,
                                    contentDescription = "Menu",
                                    tint = secondaryText.copy(alpha = 0.7f)
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false },
                                modifier = Modifier.background(
                                    if (isDarkMode) Color(0xFF1E1E1E) else Color(0xFFFFFFFF)
                                )
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.edit_task)) },
                                    onClick = {
                                        showMenu = false
                                        onClick()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.comment_blog_icon),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete_task)) },
                                    onClick = {
                                        showMenu = false
                                        onDelete()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(id = R.drawable.recycle_bin_icon),
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Middle Row: Task Title & Description
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = animatedContentColor
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                        )

                        if (!task.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = secondaryText.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                ),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Row: Time and Checkbox
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Notes,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = secondaryText.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = dueTimeText ?: "",
                                fontSize = 13.sp,
                                color = secondaryText.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                            if (task.subTasks.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { isExpanded = !isExpanded },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.chevron_direction_bottom_round_outline_icon),
                                        contentDescription = "Show sub-tasks",
                                        tint = secondaryText.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .size(16.dp)
                                            .graphicsLayer { rotationZ = arrowRotation }
                                    )
                                }
                            }
                        }

                        // Checkbox (Rounded Square)
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (task.isCompleted) Color(0xFF22C55E)
                                    else Color.Transparent
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (task.isCompleted) Color(0xFF22C55E) else checkBorderColor,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onToggleCompletion()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (task.isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Done",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded && task.subTasks.isNotEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp)
                ) {
                    task.subTasks.forEachIndexed { index, subTask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom Connector Line (L-shape)
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .fillMaxHeight()
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val strokeWidth = 1.5.dp.toPx()
                                    val connectorColor = secondaryText.copy(alpha = 0.2f)
                                    val w = size.width
                                    val h = size.height
                                    val centerX = w / 2

                                    // Vertical line: from top to center (or bottom if not last)
                                    drawLine(
                                        color = connectorColor,
                                        start = Offset(centerX, 0f),
                                        end = Offset(centerX, if (index == task.subTasks.size - 1) h / 2 else h),
                                        strokeWidth = strokeWidth
                                    )

                                    // Horizontal line to the right
                                    drawLine(
                                        color = connectorColor,
                                        start = Offset(centerX, h / 2),
                                        end = Offset(w, h / 2),
                                        strokeWidth = strokeWidth
                                    )
                                }
                            }

                            Box(modifier = Modifier.padding(vertical = 4.dp)) {
                                SubTaskItem(
                                    subTask = subTask,
                                    isDarkMode = isDarkMode,
                                    cardBg = cardBg,
                                    onToggle = { onToggleSubTask(subTask.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubTaskItem(
    subTask: SubTask,
    isDarkMode: Boolean,
    cardBg: Color,
    onToggle: () -> Unit
) {
    val primaryText = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val secondaryText = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    val completionAlpha by animateFloatAsState(
        targetValue = if (subTask.isCompleted) 0.5f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "subTaskAlpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = completionAlpha }
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = if (isDarkMode) Color.White.copy(alpha = 0.03f)
                else Color.Black.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp))
            .clickable { onToggle() },
        color = cardBg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox for Sub-task
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (subTask.isCompleted) Color(0xFF22C55E) else Color.Transparent)
                    .border(
                        1.5.dp,
                        if (subTask.isCompleted) Color(0xFF22C55E) else secondaryText.copy(alpha = 0.4f),
                        RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (subTask.isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = subTask.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (subTask.isCompleted) secondaryText.copy(alpha = 0.6f) else primaryText,
                textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else null,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AIPriorityBadge(score: Int, completionProgress: Float) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color(0xFF7B61FF).copy(alpha = leap(0.12f, 0.05f, completionProgress)))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(10.dp),
                tint = Color(0xFF7B61FF).copy(alpha = leap(1f, 0.5f, completionProgress))
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "AI: $score",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF7B61FF).copy(alpha = leap(1f, 0.5f, completionProgress))
            )
        }
    }
}

@Composable
private fun CategoryPill(category: TaskCategory, completionProgress: Float) {
    val color = getCategoryColor(category)
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = leap(0.12f, 0.05f, completionProgress)))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = category.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = leap(1f, 0.5f, completionProgress))
        )
    }
}

@Composable
private fun PriorityPill(priority: TaskPriority, completionProgress: Float) {
    val color = getPriorityColor(priority)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(color.copy(alpha = leap(0.12f,
                0.05f, completionProgress)))
            .padding(horizontal = 10.dp,
                vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = leap(1f,
                        0.5f, completionProgress)))
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = priority.label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = leap(1f, 0.5f, completionProgress))
            )
        }
    }
}

private fun leap(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

fun getPriorityColor(priority: TaskPriority): Color = when (priority) {
    TaskPriority.HIGH -> PriorityHigh
    TaskPriority.MEDIUM -> PriorityMedium
    TaskPriority.LOW -> PriorityLow
}

fun getCategoryColor(category: TaskCategory): Color = when (category) {
    TaskCategory.WORK -> CategoryWork
    TaskCategory.PERSONAL -> CategoryPersonal
    TaskCategory.STUDY -> CategoryStudy
    TaskCategory.HEALTH -> CategoryHealth
    TaskCategory.SHOPPING -> CategoryShopping
    TaskCategory.FINANCE -> CategoryFinance
    TaskCategory.HOME -> CategoryHome
    TaskCategory.OTHER -> CategoryOther
}

@Preview(showBackground = true, name = "Sub-tasks Hierarchical View")
@Composable
fun TaskCardSubTasksPreview() {
    TodoAppTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            TaskCard(
                task = Task(
                    id = "1",
                    title = "Main Project Task",
                    description = "A task with multiple sub-steps to complete.",
                    category = TaskCategory.WORK,
                    priority = TaskPriority.HIGH,
                    subTasks = listOf(
                        SubTask(title = "Initial Research & Planning", isCompleted = true),
                        SubTask(title = "Development phase alpha", isCompleted = false),
                        SubTask(title = "Bug fixing and QA", isCompleted = false)
                    )
                ),
                isDarkMode = false,
                onToggleCompletion = {},
                onToggleSubTask = {},
                onDelete = {},
                onClick = {},
                initiallyExpanded = true
            )
        }
    }
}

@Preview(showBackground = true, name = "Light Mode")
@Composable
fun TaskCardPreviewLight() {
    TodoAppTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            TaskCard(
                task = Task(
                    title = "Buy groceries",
                    description = "Milk, Eggs, Bread, and Fruits",
                    category = TaskCategory.SHOPPING,
                    priority = TaskPriority.HIGH,
                    isCompleted = false,
                    subTasks = listOf(
                        SubTask(title = "Milk", isCompleted = true),
                        SubTask(title = "Eggs", isCompleted = false),
                        SubTask(title = "Bread", isCompleted = false)
                    )
                ),
                isDarkMode = false,
                onToggleCompletion = {},
                onToggleSubTask = {},
                onDelete = {},
                onClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            TaskCard(
                task = Task(
                    title = "Finish project",
                    description = "Complete the final report",
                    category = TaskCategory.WORK,
                    priority = TaskPriority.MEDIUM,
                    isCompleted = true
                ),
                isDarkMode = false,
                onToggleCompletion = {},
                onToggleSubTask = {},
                onDelete = {},
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Dark Mode")
@Composable
fun TaskCardPreviewDark() {
    TodoAppTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                TaskCard(
                    task = Task(
                        title = "Buy groceries",
                        description = "Milk, Eggs, Bread, and Fruits",
                        category = TaskCategory.SHOPPING,
                        priority = TaskPriority.HIGH,
                        isCompleted = false
                    ),
                    isDarkMode = true,
                    onToggleCompletion = {},
                    onToggleSubTask = {},
                    onDelete = {},
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(16.dp))
                TaskCard(
                    task = Task(
                        title = "Finish project",
                        description = "Complete the final report",
                        category = TaskCategory.WORK,
                        priority = TaskPriority.MEDIUM,
                        isCompleted = true
                    ),
                    isDarkMode = true,
                    onToggleCompletion = {},
                    onToggleSubTask = {},
                    onDelete = {},
                    onClick = {}
                )
            }
        }
    }
}
