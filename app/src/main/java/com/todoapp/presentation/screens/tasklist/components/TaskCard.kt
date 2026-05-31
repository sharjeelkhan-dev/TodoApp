package com.todoapp.presentation.screens.tasklist.components
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
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
import com.todoapp.presentation.theme.*
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

    val cardBgAlpha = if (isDarkMode) {
        leap(0.15f, 0.05f, completionProgress)
    } else {
        leap(0.08f, 0.04f, completionProgress)
    }
    categoryColor.copy(alpha = cardBgAlpha)
    
    val primaryText = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val secondaryText = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val animatedContentColor by animateColorAsState(
        targetValue = if (task.isCompleted) secondaryText else primaryText,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "contentColor"
    )

    val accentColor by animateColorAsState(
        targetValue = if (task.isCompleted) Color(0xFF22C55E) else categoryColor,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "accentColor"
    )

    val checkBgColor by animateColorAsState(
        targetValue = if (task.isCompleted) Color(0xFF22C55E) else Color.Transparent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "checkBg"
    )

    val checkBorderColor by animateColorAsState(
        targetValue = if (task.isCompleted) Color(0xFF22C55E) else {
            if (isDarkMode) Color(0xFF475569) else Color(0xFFCBD5E1)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "checkBorder"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (task.isCompleted) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "iconScale"
    )

    val circleScale by animateFloatAsState(
        targetValue = if (task.isCompleted) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        label = "circleScale"
    )

    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val creationTime = timeFormat.format(task.createdAt)

    var showMenu by remember { mutableStateOf(false) }
    var actualTextWidth by remember { mutableFloatStateOf(0f) }
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "arrowRotation"
    )

    Box(modifier = modifier) {
        Column {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .combinedClickable(
                        onClick = { /* Using menu instead */ },
                        onLongClick = { showMenu = true }
                    ),
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkMode) Color(0xFF1E1E1E) else Color.White,
                shadowElevation = if (isDarkMode) 0.dp else 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left accent strip
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(accentColor)
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Check circle with bouncy interaction
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .graphicsLayer(scaleX = circleScale, scaleY = circleScale)
                                .clip(CircleShape)
                                .background(checkBgColor)
                                .border(
                                    width = 2.dp,
                                    color = checkBorderColor,
                                    shape = CircleShape
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { 
                                    onToggleCompletion() 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Done",
                                modifier = Modifier
                                    .size(14.dp)
                                    .graphicsLayer(scaleX = iconScale, scaleY = iconScale),
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = task.title,
                                        modifier = Modifier
                                            .weight(1f, fill = false)
                                            .drawWithContent {
                                                drawContent()
                                                // Animated strikethrough limited to actual visible text width
                                                if (completionProgress > 0f && actualTextWidth > 0f) {
                                                    val y = size.height / 2f + 2.dp.toPx()
                                                    drawLine(
                                                        color = animatedContentColor.copy(alpha = 0.6f),
                                                        start = Offset(0f, y),
                                                        end = Offset(actualTextWidth * completionProgress, y),
                                                        strokeWidth = 1.5.dp.toPx()
                                                    )
                                                }
                                            },
                                        onTextLayout = { textLayoutResult ->
                                            // Get the right edge of the text on the first line to handle ellipsis correctly
                                            actualTextWidth = if (textLayoutResult.lineCount > 0) {
                                                textLayoutResult.getLineRight(0)
                                            } else {
                                                0f
                                            }
                                        },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = animatedContentColor
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    
                                    if (task.subTasks.isNotEmpty()) {
                                        IconButton(
                                            onClick = { isExpanded = !isExpanded },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.chevron_direction_bottom_round_outline_icon),
                                                contentDescription = "Show sub-tasks",
                                                tint = if (isDarkMode) Color.White else Color.Black,
                                                modifier = Modifier
                                                    .size(20.dp)
                                                    .graphicsLayer(rotationZ = arrowRotation)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = creationTime,
                                    fontSize = 10.sp,
                                    color = secondaryText.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                            if (task.description.isNotEmpty()) {
                                Text(
                                    text = task.description,
                                    fontSize = 12.sp,
                                    color = secondaryText.copy(alpha = 1f - (completionProgress * 0.3f)),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CategoryPill(task.category, completionProgress)
                                PriorityPill(task.priority, completionProgress)
                                task.aiPriorityScore?.let { score ->
                                    AIPriorityBadge(score, completionProgress)
                                }
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
                        .padding(start = 18.dp) // Perfectly aligns with parent's check circle center
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
                                    val strokeWidth = 2.dp.toPx()
                                    val connectorColor = categoryColor.copy(alpha = 0.4f)
                                    val w = size.width
                                    val h = size.height
                                    
                                    // Vertical line from top to center
                                    drawLine(
                                        color = connectorColor,
                                        start = Offset(w / 2, 0f),
                                        end = Offset(w / 2, if (index == task.subTasks.size - 1) h / 2 else h),
                                        strokeWidth = strokeWidth
                                    )
                                    
                                    // Horizontal line to the right
                                    drawLine(
                                        color = connectorColor,
                                        start = Offset(w / 2, h / 2),
                                        end = Offset(w, h / 2),
                                        strokeWidth = strokeWidth
                                    )

                                    // Arrow head pointing to sub-task card
                                    val arrowSize = 4.dp.toPx()
                                    val path = Path().apply {
                                        moveTo(w - arrowSize, h / 2 - arrowSize / 1.5f)
                                        lineTo(w, h / 2)
                                        lineTo(w - arrowSize, h / 2 + arrowSize / 1.5f)
                                    }
                                    drawPath(path, connectorColor, style = Stroke(strokeWidth, cap = StrokeCap.Round))
                                }
                            }

                            // Sub-task card (Flat, matching the parent's clean design)
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDarkMode) Color(0xFF1E1E1E) else Color.White,
                                shadowElevation = 0.dp // Shadow removed as requested
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Sub-task Left accent strip
                                    Box(
                                        modifier = Modifier
                                            .width(6.dp)
                                            .fillMaxHeight()
                                            .background(
                                                if (subTask.isCompleted) Color(0xFF22C55E)
                                                else categoryColor.copy(alpha = 0.7f)
                                            )
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp), // Reduced padding
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Small check circle
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(if (subTask.isCompleted) Color(0xFF22C55E) else Color.Transparent)
                                                .border(1.5.dp, if (subTask.isCompleted) Color(0xFF22C55E) else secondaryText.copy(alpha = 0.4f), CircleShape)
                                                .clickable { onToggleSubTask(subTask.id) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (subTask.isCompleted) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.width(8.dp)) // Reduced spacer

                                        Text(
                                            text = subTask.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (subTask.isCompleted) secondaryText.copy(alpha = 0.6f) else primaryText,
                                            textDecoration = if (subTask.isCompleted) TextDecoration.LineThrough else null,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(if (isDarkMode)
                Color(0xFF1E1E1E) else Color(0xFFFFFFFF))
        ) {
            DropdownMenuItem(
                text = { Text("Edit Task") },
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
                text = { Text("Delete Task") },
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

@Composable
private fun AIPriorityBadge(score: Int, completionProgress: Float) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFF7B61FF).copy(alpha = leap(0.12f, 0.05f, completionProgress)))
            .padding(horizontal = 8.dp, vertical = 4.dp)
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
            .clip(RoundedCornerShape(7.dp))
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
