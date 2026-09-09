package com.todoapp.presentation.screens.focus

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.todoapp.R
import com.todoapp.domain.model.Task
import com.todoapp.presentation.theme.PoppinsFontFamily
import com.todoapp.presentation.theme.TodoAppTheme
import com.todoapp.presentation.theme.TodoTypography
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FocusScreen(
    isDarkMode: Boolean,
    viewModel: FocusViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    FocusScreenContent(
        isDarkMode = isDarkMode,
        currentSessionType = state.currentSessionType,
        baseDurationMinutes = state.baseDurationMinutes,
        timeRemaining = state.timeRemaining,
        timerRunning = state.timerRunning,
        selectedTask = state.selectedTask,
        tasks = state.tasks,
        onAdjustTime = { minutes -> viewModel.onEvent(FocusEvent.AdjustTime(minutes)) },
        onSelectTask = { task -> viewModel.onEvent(FocusEvent.SelectTask(task)) },
        onStartPause = { viewModel.onEvent(FocusEvent.StartPause) },
        onReset = { viewModel.onEvent(FocusEvent.Reset) }
    )
}

// ==========================================
// 2. STATELESS CONTENT (REUSABLE FOR PREVIEW)
// ==========================================

@Composable
fun FocusScreenContent(
    isDarkMode: Boolean,
    currentSessionType: SessionType,
    baseDurationMinutes: Int,
    timeRemaining: Int,
    timerRunning: Boolean,
    selectedTask: Task?,
    tasks: List<Task>,
    onAdjustTime: (Int) -> Unit,
    onSelectTask: (Task?) -> Unit,
    onStartPause: () -> Unit,
    onReset: () -> Unit
) {
    val brandColor = Color(0xFF6C5CE7)
    val bgColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFFBFBF9)
    val textColor = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val subTextColor = if (isDarkMode) Color(0xFFAAAAAA) else Color(0xFF888888)

    val infiniteTransition = rememberInfiniteTransition(label = "ambient")
    val driftY by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Floating Ambient Background Elements (Adjusted inside screen bounds)
        Box(modifier = Modifier.fillMaxSize().alpha(0.4f)) {
            FloatingElement(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset { IntOffset(20.dp.roundToPx(), (100.dp + driftY.dp).roundToPx()) },
                color = Color(0xFFE91E63),
                isDarkMode = isDarkMode
            )
            FloatingElement(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset { IntOffset((-16).dp.roundToPx(), ((-160).dp - driftY.dp).roundToPx()) },
                color = brandColor,
                isDarkMode = isDarkMode
            )
            FloatingElement(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset { IntOffset((-16).dp.roundToPx(), (180.dp - driftY.dp).roundToPx()) },
                color = Color(0xFFFF9800),
                isDarkMode = isDarkMode
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Session Title Label
            Text(
                text = when (currentSessionType) {
                    SessionType.WORK -> stringResource(R.string.work_time)
                    SessionType.SHORT_BREAK -> stringResource(R.string.short_break)
                    SessionType.LONG_BREAK -> stringResource(R.string.long_break)
                },
                style = TodoTypography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Circular Timer & Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!timerRunning) {
                    AdjustButton(
                        icon = Icons.Rounded.Remove,
                        onClick = { onAdjustTime(-5) },
                        isDarkMode = isDarkMode
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp)
                ) {
                    val totalSeconds = baseDurationMinutes * 60
                    val progress by animateFloatAsState(
                        targetValue = if (totalSeconds > 0) timeRemaining.toFloat() / totalSeconds else 0f,
                        label = "Timer Progress"
                    )

                    // Timer Ring
                    TimerRing(
                        progress = progress,
                        timerRunning = timerRunning,
                        isDarkMode = isDarkMode,
                        brandColor = brandColor,
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val minutes = timeRemaining / 60
                        val seconds = timeRemaining % 60
                        Text(
                            text = "%02d:%02d".format(minutes, seconds),
                            fontSize = 58.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor,
                            lineHeight = 58.sp,
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                        Text(
                            text = if (timerRunning) "Focusing..." else "Ready?",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = subTextColor,
                            style = TextStyle(
                                fontFamily = PoppinsFontFamily,
                                platformStyle = PlatformTextStyle(includeFontPadding = false)
                            )
                        )
                    }
                }
                if (!timerRunning) {
                    Spacer(modifier = Modifier.width(16.dp))
                    AdjustButton(
                        icon = Icons.Rounded.Add,
                        onClick = { onAdjustTime(5) },
                        isDarkMode = isDarkMode
                    )
                }
            }

            // Task Selection Menu
            TaskSelectorCard(
                selectedTask = selectedTask,
                tasks = tasks,
                onSelectTask = onSelectTask,
                isDarkMode = isDarkMode,
                brandColor = brandColor
            )

            // Primary Control Actions (Start/Pause & Reset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PremiumButton(
                    text = if (timerRunning) stringResource(R.string.pause) else stringResource(R.string.start),
                    onClick = onStartPause,
                    containerColor = brandColor,
                    contentColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                PremiumButton(
                    text = stringResource(R.string.reset_timer),
                    onClick = onReset,
                    containerColor = Color.Transparent,
                    contentColor = brandColor,
                    borderColor = brandColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ==========================================
// 3. REUSABLE UI SUB-COMPONENTS
// ==========================================

@Composable
fun TimerRing(
    progress: Float,
    timerRunning: Boolean,
    isDarkMode: Boolean,
    brandColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2
        val center = size.center
        val arcRect = Rect(
            center = center,
            radius = radius
        )

        // 1. Dark Inner Circle Background
        drawCircle(
            color = if (isDarkMode) Color(0xFF16161E) else Color(0xFFF0F0F5),
            radius = radius - (strokeWidth / 2),
            center = center
        )

        // 2. Track Ring (Unfilled Track)
        drawCircle(
            color = if (isDarkMode) Color(0xFF282838) else Color(0xFFE2E2EC),
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // 3. Active Progress Arc & Thumb Dot
        if (progress > 0f) {
            val sweepAngle = progress * 360f

            drawArc(
                color = brandColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcRect.topLeft,
                size = arcRect.size,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // 4. White Cap Thumb Dot
            val angleInRadians = Math.toRadians((sweepAngle - 90).toDouble())
            val thumbX = center.x + radius * cos(angleInRadians).toFloat()
            val thumbY = center.y + radius * sin(angleInRadians).toFloat()

            drawCircle(
                color = Color.White,
                radius = strokeWidth * 0.4f,
                center = Offset(thumbX, thumbY)
            )
        }
    }
}

@Composable
fun FloatingElement(modifier: Modifier = Modifier, color: Color, isDarkMode: Boolean) {
    val cardBg = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    Box(
        modifier = modifier
            .size(width = 80.dp, height = 32.dp)
            .shadow(if (isDarkMode) 4.dp else 12.dp, RoundedCornerShape(12.dp))
            .background(cardBg, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(40.dp)
                        .background(color.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(20.dp)
                        .background(color.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
fun AdjustButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    isDarkMode: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.85f else 1f, label = "scale")
    val brandColor = Color(0xFF6C5CE7)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = CircleShape,
        color = if (isDarkMode) brandColor.copy(alpha = 0.15f) else brandColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, brandColor.copy(alpha = 0.2f)),
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(if (isDarkMode) 0.dp else 2.dp, CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun TaskSelectorCard(
    selectedTask: Task?,
    tasks: List<Task>,
    onSelectTask: (Task?) -> Unit,
    isDarkMode: Boolean,
    brandColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val cardBg = if (isDarkMode) Color(0xFF1E1E1E) else Color.White

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.tasks_label),
            style = TodoTypography.labelLarge,
            color = Color.Gray,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        Box {
            Surface(
                onClick = { expanded = true },
                shape = RoundedCornerShape(16.dp),
                color = cardBg,
                shadowElevation = 8.dp,
                border = BorderStroke(1.dp, if (expanded) brandColor else Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.notepad_icon),
                            contentDescription = null,
                            tint = brandColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = selectedTask?.title ?: stringResource(R.string.select_task_to_focus),
                            style = TodoTypography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = if (selectedTask == null) Color.Gray else if (isDarkMode) Color.White else Color.Black
                        )
                    }
                    Icon(
                        painter = painterResource(id = R.drawable.chevron_direction_bottom_round_outline_icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .background(cardBg)
            ) {
                DropdownMenuItem(
                    text = { Text("None", style = TodoTypography.bodyLarge) },
                    onClick = {
                        onSelectTask(null)
                        expanded = false
                    }
                )
                tasks.forEach { task ->
                    DropdownMenuItem(
                        text = { Text(task.title, style = TodoTypography.bodyLarge) },
                        onClick = {
                            onSelectTask(task)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "scale")

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp),
        border = borderColor?.let { BorderStroke(2.dp, it) },
        modifier = modifier
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                if (containerColor != Color.Transparent) 8.dp else 0.dp,
                RoundedCornerShape(16.dp),
                spotColor = containerColor
            ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            style = TodoTypography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

// ==========================================
// 4. JETPACK COMPOSE PREVIEW PROVIDER
// ==========================================

data class FocusPreviewData(
    val isDarkMode: Boolean,
    val currentSessionType: SessionType,
    val baseDurationMinutes: Int,
    val timeRemaining: Int,
    val timerRunning: Boolean,
    val selectedTask: Task?,
    val tasks: List<Task>
)

class FocusPreviewParameterProvider : PreviewParameterProvider<FocusPreviewData> {
    override val values: Sequence<FocusPreviewData> = sequenceOf(
        FocusPreviewData(
            isDarkMode = false,
            currentSessionType = SessionType.WORK,
            baseDurationMinutes = 25,
            timeRemaining = 1500,
            timerRunning = false,
            selectedTask = Task(id = "1", title = "Refactor Focus Screen UI"),
            tasks = listOf(
                Task(id = "1", title = "Refactor Focus Screen UI"),
                Task(id = "2", title = "Fix Room DB Issue")
            )
        ),
        FocusPreviewData(
            isDarkMode = true,
            currentSessionType = SessionType.WORK,
            baseDurationMinutes = 25,
            timeRemaining = 800,
            timerRunning = true,
            selectedTask = Task(id = "2", title = "Fix Room DB Issue"),
            tasks = listOf(
                Task(id = "1", title = "Refactor Focus Screen UI"),
                Task(id = "2", title = "Fix Room DB Issue")
            )
        )
    )
}

@Preview(name = "Light Mode", showBackground = true, backgroundColor = 0xFFFBFBF9)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF121212
)
@Composable
fun FocusScreenPreview(
    @PreviewParameter(FocusPreviewParameterProvider::class) previewData: FocusPreviewData
) {
    TodoAppTheme(darkTheme = previewData.isDarkMode, dynamicColor = false) {
        FocusScreenContent(
            isDarkMode = previewData.isDarkMode,
            currentSessionType = previewData.currentSessionType,
            baseDurationMinutes = previewData.baseDurationMinutes,
            timeRemaining = previewData.timeRemaining,
            timerRunning = previewData.timerRunning,
            selectedTask = previewData.selectedTask,
            tasks = previewData.tasks,
            onAdjustTime = {},
            onSelectTask = {},
            onStartPause = {},
            onReset = {}
        )
    }
}