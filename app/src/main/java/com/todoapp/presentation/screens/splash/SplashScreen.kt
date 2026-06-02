package com.todoapp.presentation.screens.splash
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todoapp.R
import com.todoapp.presentation.theme.TodoAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    isDarkMode: Boolean,
    tasksCount: Int,
    doneCount: Int,
    onNavigateToHome: () -> Unit
) {
    val progressAnim = remember { Animatable(0f) }
    val badgesAlpha = remember { Animatable(0f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.8f) }

    LaunchedEffect(key1 = true) {
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(durationMillis = 800))
        }
        launch {
            logoScale.animateTo(1f, animationSpec = tween(durationMillis = 800))
        }
        launch {
            delay(400)
            badgesAlpha.animateTo(1f, animationSpec = tween(durationMillis = 1000))
        }
        launch {
            for (i in 1..100) {
                progressAnim.snapTo(i / 100f)
                delay(30)
            }
        }
        delay(3500)
        onNavigateToHome()
    }

    SplashScreenContent(
        isDarkMode = isDarkMode,
        progress = progressAnim.value,
        badgesAlpha = badgesAlpha.value,
        logoAlpha = logoAlpha.value,
        tasksCount = tasksCount,
        doneCount = doneCount
    )
}

@Composable
fun SplashScreenContent(
    isDarkMode: Boolean,
    progress: Float,
    badgesAlpha: Float,
    logoAlpha: Float,
    tasksCount: Int,
    doneCount: Int
) {
    // --- Customizable Offsets for Floating Cards ---
    val card1OffsetX = 30.dp
    val card1OffsetY = (-250).dp
    
    val card2OffsetX = (-20).dp
    val card2OffsetY = (-270).dp
    
    val card3OffsetX = 15.dp
    val card3OffsetY = (-80).dp
    
    val card4OffsetX = (-30).dp
    val card4OffsetY = (-10).dp

    val bgColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFFBFBFB)
    val primaryColor = Color(0xFF7B61FF)
    val textColor = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val subTextColor = if (isDarkMode) Color(0xFFAAAAAA) else Color(0xFF888888)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            contentAlignment = Alignment.Center
        ) {
            // --- Floating Cards ---
            Box(modifier = Modifier.fillMaxSize().alpha(badgesAlpha)) {
                FloatingCard(
                    iconColor = Color(0xFFE91E63),
                    modifier = Modifier.align(Alignment.CenterStart).offset(x = card1OffsetX, y = card1OffsetY)
                )
                FloatingCard(
                    iconColor = Color(0xFF4CAF50),
                    modifier = Modifier.align(Alignment.CenterEnd).offset(x = card2OffsetX, y = card2OffsetY)
                )
                FloatingCard(
                    iconColor = Color(0xFF7B61FF),
                    modifier = Modifier.align(Alignment.CenterStart).offset(x = card3OffsetX, y = card3OffsetY)
                )
                FloatingCard(
                    iconColor = Color(0xFFFF9800),
                    modifier = Modifier.align(Alignment.CenterEnd).offset(x = card4OffsetX, y = card4OffsetY)
                )
            }

            // --- Center Content ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(logoAlpha)
            ) {
                // Main App Icon
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(16.dp, RoundedCornerShape(40.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF6C52EE), Color(0xFF8E75FF)),
                                    start = Offset.Zero,
                                    end = Offset.Infinite
                                ),
                                shape = RoundedCornerShape(40.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource
                                (id = R.drawable.notepad_icon),
                            contentDescription = "Logo",
                            tint = Color.White,
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    // Notification Badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 6.dp, y = (-8).dp)
                            .size(32.dp)
                            .background(Color(0xFFE53935),
                                CircleShape)
                            .shadow(4.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("3", color = Color.White, fontSize = 16.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // App Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        modifier = Modifier.offset(y = 60.dp),
                        text = "My",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Normal,
                        color = textColor
                    )
                    Text(
                        modifier = Modifier.offset(y = 60.dp),
                        text = "Tasks",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle
                Text(modifier = Modifier.offset(y = 50.dp),
                    text = "Plan it. Do it. Own it.",
                    fontSize = 16.sp,
                    color = subTextColor,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Progress Bar
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(6.dp)
                        .background(primaryColor.copy(alpha = 0.1f), CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(6.dp)
                            .background(primaryColor,
                                CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(modifier = Modifier.offset(y = (-10).dp),
                    text = "Almost ready...",
                    fontSize = 14.sp,
                    color = subTextColor
                )
            }

            // --- Bottom Stats ---
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 64.dp, start = 32.dp, end = 32.dp)
                    .alpha(logoAlpha),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(tasksCount.toString(), "TASKS", Color(0xFF3F51B5), isDarkMode, Modifier.weight(1f))
                StatCard(doneCount.toString(), "DONE", Color(0xFF009688), isDarkMode, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FloatingCard(iconColor: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 12.dp,
        color = Color.White,
        modifier = modifier
            .offset(y = (-35).dp)
            .size(width = 110.dp, height = 44.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(iconColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .height(5.dp)
                        .width(50.dp)
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(2.dp))
                )
                Box(
                    modifier = Modifier
                        .height(5.dp)
                        .width(30.dp)
                        .background(Color(0xFFF0F0F0), RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
fun StatCard(value: String, label: String, valueColor: Color, isDarkMode: Boolean, modifier: Modifier = Modifier) {
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = if (isDarkMode) 4.dp else 12.dp,
        color = cardColor,
        modifier = modifier
            .height(110.dp)
            .offset(y = (-10).dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Preview(showBackground = true, name = "Splash Screen Light", showSystemUi = true)
@Composable
fun SplashScreenLightPreview() {
    TodoAppTheme(darkTheme = false, dynamicColor = false) {
        SplashScreenContent(
            isDarkMode = false,
            progress = 0.7f,
            badgesAlpha = 1f,
            logoAlpha = 1f,
            tasksCount = 12,
            doneCount = 8
        )
    }
}

@Preview(showBackground = true, name = "Splash Screen Dark", showSystemUi = true)
@Composable
fun SplashScreenDarkPreview() {
    TodoAppTheme(darkTheme = true, dynamicColor = false) {
        SplashScreenContent(
            isDarkMode = true,
            progress = 0.7f,
            badgesAlpha = 1f,
            logoAlpha = 1f,
            tasksCount = 12,
            doneCount = 8
        )
    }
}
