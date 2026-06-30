package com.todoapp.presentation.screens.addedittask

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todoapp.R
import com.todoapp.domain.model.SubTask
import com.todoapp.domain.model.TaskCategory
import com.todoapp.domain.model.TaskPriority
import com.todoapp.presentation.screens.tasklist.components.getPriorityColor
import com.todoapp.presentation.theme.PoppinsFontFamily
import com.todoapp.presentation.theme.TodoAppTheme
import com.todoapp.presentation.theme.getCategoryColor
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    state: AddEditTaskState,
    isDarkMode: Boolean,
    onEvent: (AddEditTaskEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = state.dueDate?.time
    )
    val timePickerState = rememberTimePickerState(
        initialHour = state.dueTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 12,
        initialMinute = state.dueTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0
    )

    val categoryColor = getCategoryColor(state.category)
    val bgColor = if (isDarkMode) Color(0xFF1C1B21) else Color(0xFFFBFBF9)
    val cardColor = if (isDarkMode) Color(0xFF231F26) else Color(0xFFF5F5F7)
    val cardBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.03f) else Color.Black.copy(alpha = 0.05f)
    val primaryText = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val secondaryText = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val brandColor = Color(0xFF7B61FF)
    val dividerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFEEEEEE)

    val categoryRows = remember { 
        listOf(
            TaskCategory.entries.take(4),
            TaskCategory.entries.drop(4)
        )
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onNavigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = bgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(paddingValues.calculateTopPadding() + 16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDarkMode) Color(0xFF1E1E1E) else Color.White,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, dividerColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Back", tint = secondaryText, modifier = Modifier.size(24.dp))
                    }
                }
                Text(
                    text = if (state.isEditing) stringResource(R.string.edit_task) else stringResource(R.string.new_task),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        fontSize = 26.sp
                    ),
                    modifier = Modifier.weight(1f).offset(x = 75.dp)
                )
            }

            // ─── Title Section ──────────────────
            Column {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = cardColor,
                    border = BorderStroke(1.dp, cardBorderColor)
                ) {
                    Column {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    stringResource(R.string.title_label), 
                                    color = brandColor, 
                                    fontSize = 14.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    letterSpacing = 0.5.sp,
                                    fontFamily = PoppinsFontFamily
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    painter = painterResource(id = R.drawable.comment_blog_icon), 
                                    contentDescription = null, 
                                    tint = secondaryText.copy(alpha = 0.5f), 
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Box {
                                if (state.title.isEmpty()) {
                                    Text(
                                        stringResource(R.string.title_hint), 
                                        color = secondaryText.copy(alpha = 0.4f), 
                                        fontSize = 16.sp, 
                                        fontWeight = FontWeight.Normal,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                                BasicTextField(
                                    value = state.title, 
                                    onValueChange = { if (it.length <= 60) onEvent(AddEditTaskEvent.TitleChanged(it)) }, 
                                    modifier = Modifier.fillMaxWidth(), 
                                    textStyle = TextStyle(
                                        color = primaryText, 
                                        fontSize = 16.sp, 
                                        fontWeight = FontWeight.SemiBold,
                                        fontFamily = PoppinsFontFamily
                                    ), 
                                    singleLine = true, 
                                    cursorBrush = SolidColor(brandColor)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = "${state.title.length}/60",
                    modifier = Modifier.align(Alignment.End).padding(top = 6.dp, end = 4.dp),
                    color = secondaryText,
                    fontSize = 11.sp,
                    fontFamily = PoppinsFontFamily
                )
            }

            // ─── Description Section ──────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = cardColor,
                border = BorderStroke(1.dp, cardBorderColor)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        stringResource(R.string.description_label), 
                        color = brandColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontFamily = PoppinsFontFamily
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box {
                        if (state.description.isEmpty()) {
                            Text(
                                stringResource(R.string.desc_hint), 
                                color = secondaryText.copy(alpha = 0.4f), 
                                fontSize = 15.sp,
                                fontFamily = PoppinsFontFamily
                            )
                        }
                        BasicTextField(
                            value = state.description,
                            onValueChange = { onEvent(AddEditTaskEvent.DescriptionChanged(it)) }, 
                            modifier = Modifier.fillMaxWidth().height(80.dp), 
                            textStyle = TextStyle(
                                color = primaryText, 
                                fontSize = 15.sp,
                                fontFamily = PoppinsFontFamily
                            ), 
                            cursorBrush = SolidColor(brandColor)
                        )
                    }
                }
            }

            HorizontalDivider(color = dividerColor.copy(alpha = 0.5f),
                thickness = 1.dp)

            Text(stringResource(R.string.category_label), color = secondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                categoryRows.forEach { rowCategories ->
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        rowCategories.forEach { category ->
                            CategoryItem(
                                category = category,
                                isSelected = state.category == category,
                                cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White,
                                secondaryText = secondaryText,
                                dividerColor = dividerColor,
                                modifier = Modifier.weight(1f),
                                onClick = { onEvent(AddEditTaskEvent.CategoryChanged(category)) }
                            )
                        }
                    }
                }
            }

            Text(stringResource(R.string.priority_label), color = secondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TaskPriority.entries.forEach { priority ->
                    PriorityChip(priority = priority,
                        isSelected = state.priority == priority,
                        cardColor = if (isDarkMode)
                            Color(0xFF1E1E1E) else
                                Color.White,
                        secondaryText = secondaryText,
                        dividerColor = dividerColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onEvent(AddEditTaskEvent.PriorityChanged(priority)) })
                }
            }

            Text(stringResource(R.string.due_date_time), color = secondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DateTimeCard(painter = painterResource(id = R.drawable.date_line), label = stringResource(R.string.date), value = state.dueDate?.let { dateFormat.format(it) } ?: stringResource(R.string.set_date), cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White, primaryText = primaryText, secondaryText = secondaryText, brandColor = brandColor, dividerColor = dividerColor, modifier = Modifier.weight(1.2f), onClick = { onEvent(AddEditTaskEvent.ToggleDatePicker) })
                DateTimeCard(painter = painterResource(id = R.drawable.time_03), label = stringResource(R.string.time), value = state.dueTime ?: stringResource(R.string.set_time), cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White, primaryText = primaryText, secondaryText = secondaryText, brandColor = brandColor, dividerColor = dividerColor, modifier = Modifier.weight(1f), onClick = { onEvent(AddEditTaskEvent.ToggleTimePicker) })
            }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = if (isDarkMode) Color(0xFF1E1E1E) else Color.White, shadowElevation = 0.dp, border = BorderStroke(1.dp, dividerColor)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(if (isDarkMode) Color(0xFF3B2A1A) else Color(0xFFFFF7ED)), contentAlignment = Alignment.Center) {
                        Icon(painter = painterResource(id = R.drawable.remind), contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.reminder), color = primaryText, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(stringResource(R.string.reminder_desc), color = secondaryText, fontSize = 12.sp)
                    }
                    Icon(
                        painter = painterResource(
                            id = if (state.isReminderEnabled) R.drawable.toggle_on_line_icon else R.drawable.toggle_off_line_icon
                        ),
                        contentDescription = "Toggle Reminder",
                        tint = if (state.isReminderEnabled) brandColor else secondaryText,
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onEvent(AddEditTaskEvent.ToggleReminder(!state.isReminderEnabled)) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.sub_tasks), color = secondaryText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { onEvent(AddEditTaskEvent.AddSubTask) }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.add_sub_task))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.subTasks.forEachIndexed { index, subTask ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = cardColor,
                        border = BorderStroke(1.dp, cardBorderColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sub-task Icon (Simplified for Input)
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = secondaryText.copy(alpha = 0.4f),
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Box(modifier = Modifier.weight(1f)) {
                                if (subTask.title.isEmpty()) {
                                    Text(
                                        stringResource(R.string.sub_task_hint),
                                        color = secondaryText.copy(alpha = 0.4f),
                                        fontSize = 14.sp,
                                        fontFamily = PoppinsFontFamily
                                    )
                                }
                                BasicTextField(
                                    value = subTask.title,
                                    onValueChange = { onEvent(AddEditTaskEvent.SubTaskTitleChanged(subTask.id, it)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(
                                        color = primaryText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = PoppinsFontFamily
                                    ),
                                    cursorBrush = SolidColor(brandColor)
                                )
                            }

                            IconButton(
                                onClick = { onEvent(AddEditTaskEvent.RemoveSubTask(subTask.id)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.recycle_bin_icon),
                                    contentDescription = "Delete",
                                    tint = Color.Red.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ─── Action Button ──────────────────
            Button(
                onClick = { onEvent(AddEditTaskEvent.Save) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = brandColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 3.dp
                    )
                } else {
                    Text(
                        text = if (state.isEditing) stringResource(R.string.update_task) else stringResource(R.string.add_task),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding() + 40.dp))
        }
    }

    // Dialogs
    if (state.showDatePicker) {
        DatePickerDialog(onDismissRequest =
            { onEvent(AddEditTaskEvent.ToggleDatePicker) },
            confirmButton = { TextButton(onClick =
                { datePickerState.selectedDateMillis?.let {
                    onEvent(AddEditTaskEvent.DueDateChanged(Date(it))) }
                }) { Text(stringResource(R.string.ok)) } }, dismissButton =
                { TextButton(onClick =
                    { onEvent(AddEditTaskEvent.ToggleDatePicker) })
                { Text(stringResource(R.string.cancel)) } }) { DatePicker(state =
            datePickerState) }
    }
    if (state.showTimePicker) {
        androidx.compose.material3.AlertDialog(onDismissRequest =
            { onEvent(AddEditTaskEvent.ToggleTimePicker) },
            confirmButton = { TextButton(onClick = { onEvent(AddEditTaskEvent.DueTimeChanged(String.format(java.util.Locale.getDefault(), "%02d:%02d", timePickerState.hour, timePickerState.minute))) }) { Text(stringResource(R.string.ok)) } }, dismissButton = { TextButton(onClick = { onEvent(AddEditTaskEvent.ToggleTimePicker) }) { Text(stringResource(R.string.cancel)) } }, text = { TimePicker(state = timePickerState) })
    }
}

@Composable
fun CategoryItem(
    category: TaskCategory, 
    isSelected: Boolean, 
    cardColor: Color, 
    secondaryText: Color, 
    dividerColor: Color, 
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val iconRes = when(category) {
        TaskCategory.WORK -> R.drawable.work
        TaskCategory.PERSONAL -> R.drawable.person
        TaskCategory.STUDY -> R.drawable.book_open
        TaskCategory.HEALTH -> R.drawable.health_and_safety
        TaskCategory.SHOPPING -> R.drawable.shop
        TaskCategory.FINANCE -> R.drawable.money_cash_bag_dollar_bag_payment_cash_money_finance
        TaskCategory.HOME -> R.drawable.home
        TaskCategory.OTHER -> R.drawable.star
    }
    val tint = getCategoryColor(category)
    val selectedBg = tint.copy(alpha = 0.15f)
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) selectedBg else cardColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) tint else dividerColor
        ),
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
            Icon(painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = tint)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = category.label,
                color = if (isSelected) tint
                else secondaryText,
                fontSize = 11.sp,
                fontWeight = if (isSelected)
                    FontWeight.Bold
                else FontWeight.Medium)
        }
    }
}

@Composable
fun PriorityChip(
    priority: TaskPriority,
    isSelected: Boolean,
    cardColor: Color,
    secondaryText: Color,
    dividerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = getPriorityColor(priority)
    val selectedBg = color.copy(alpha = 0.15f)
    
    Surface(
        onClick = onClick,
        modifier = modifier.height(46.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) selectedBg else cardColor,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) color else dividerColor
        ),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = color)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = priority.label,
                color = if (isSelected) color else secondaryText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun DateTimeCard(painter: Painter,
                 label: String, value: String,
                 cardColor: Color, primaryText: Color,
                 secondaryText: Color, brandColor: Color,
                 dividerColor: Color, modifier: Modifier = Modifier,
                 onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = cardColor, shadowElevation = 0.dp,
        border = BorderStroke(1.dp, dividerColor)) {
        Row(modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp)).
                background(brandColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center) {
                Icon(painter = painter,
                    contentDescription = null,
                    tint = brandColor,
                    modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, color = secondaryText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp)
                Text(value, color = primaryText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1)
            }
        }
    }
}
@Preview(showBackground = true, name = "Light Mode - Editing")
@Composable
fun AddEditTaskScreenPreviewLight() {
    TodoAppTheme(darkTheme = false) {
        AddEditTaskScreen(
            state = AddEditTaskState(
                title = "Design System Update",
                description = "Revise the primary color palette and update the component library documentation.",
                category = TaskCategory.WORK,
                priority = TaskPriority.HIGH,
                dueDate = Date(),
                dueTime = "14:30",
                isReminderEnabled = true,
                isEditing = true
            ),
            isDarkMode = false,
            onEvent = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Single Sub-task Card")
@Composable
fun SingleSubTaskPreview() {
    TodoAppTheme(darkTheme = false) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF5F5F7),
            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Example sub-task step",
                    color = Color(0xFF1A1A1A),
                    fontSize = 14.sp,
                    fontFamily = PoppinsFontFamily,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    painter = painterResource(id = R.drawable.recycle_bin_icon),
                    contentDescription = null,
                    tint = Color.Red.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Add Sub-task Section Preview")
@Composable
fun AddSubTaskSectionPreview() {
    TodoAppTheme(darkTheme = false) {
        Surface(modifier = Modifier.padding(16.dp)) {
            AddEditTaskScreen(
                state = AddEditTaskState(
                    title = "Work Project",
                    description = "Project details and tasks.",
                    subTasks = listOf(
                        SubTask(title = "Task 1: Research", isCompleted = true),
                        SubTask(title = "Task 2: Development", isCompleted = false),
                        SubTask(title = "Task 3: Testing", isCompleted = false)
                    ),
                    isEditing = true
                ),
                isDarkMode = false,
                onEvent = {},
                onNavigateBack = {}
            )
        }
    }
}
