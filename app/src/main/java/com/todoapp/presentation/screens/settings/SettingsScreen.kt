package com.todoapp.presentation.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todoapp.R
import com.todoapp.presentation.theme.TodoAppTheme

/**
 * Settings screen matching the provided UI design precisely.
 * Correctly handles theme-aware colors and loading indicator placement.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsState,
    isDarkMode: Boolean,
    onToggleDarkMode: (Boolean) -> Unit,
    onEvent: (SettingsEvent) -> Unit,
    onNavigateBack: () -> Unit,
    onSignedOut: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isPreview = LocalInspectionMode.current

    LaunchedEffect(state.isSignedOut) {
        if (state.isSignedOut) onSignedOut()
    }

    if (!isPreview) {
        LaunchedEffect(state.message) {
            state.message?.let {
                snackbarHostState.showSnackbar(it)
                onEvent(SettingsEvent.ClearMessage)
            }
        }
    }

    val bgColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFFBFBF9)
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val primaryText = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val secondaryText = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val brandColor = Color(0xFF7B61FF)
    val dividerColor = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFF2F2F2)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = bgColor,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(paddingValues.calculateTopPadding() + 16.dp))

            // ─── Header ──────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = cardColor,
                    shadowElevation = if (isDarkMode) 0.dp else 6.dp,
                    border = BorderStroke(1.dp, dividerColor)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = primaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        fontSize = 25.sp
                    )
                )
            }

            // ─── Profile Section ──────────────────
            state.userProfile?.let { profile ->
                SettingsGroupCard(cardColor, dividerColor) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(brandColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (profile.displayName.takeIf { it.isNotBlank() } ?: profile.email).take(1).uppercase(),
                                color = brandColor,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column {
                            Text(
                                text = profile.displayName.takeIf { it.isNotBlank() } ?: "User",
                                color = primaryText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = profile.email,
                                color = secondaryText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }


            // ─── Appearance Section ──────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsSectionHeader("APPEARANCE", secondaryText)
                SettingsGroupCard(cardColor, dividerColor) {
                    SettingsToggleItem(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Switch between light and dark theme",
                        isChecked = isDarkMode,
                        iconBgColor = if (isDarkMode) Color(0xFF2D2D4D) else Color(0xFFEBEBFF),
                        iconTintColor = if (isDarkMode) Color.White else brandColor,
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        brandColor = brandColor,
                        onCheckedChange = onToggleDarkMode
                    )
                }
            }

            // ─── Data Section ──────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsSectionHeader("DATA MANAGEMENT", secondaryText)
                SettingsGroupCard(cardColor, dividerColor) {
                    SettingsClickItem(
                        painter = painterResource(id = R.drawable.data_update_icon),
                        title = "Backup Data",
                        subtitle = state.lastBackupTime ?: "Save all tasks to cloud",
                        isLoading = state.isBackupLoading,
                        iconBgColor = if (isDarkMode) Color(0xFF1E2D4D) else Color(0xFFE7F0FE),
                        iconTintColor = Color(0xFF2563EB),
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        brandColor = brandColor,
                        onClick = { onEvent(SettingsEvent.BackupData) }
                    )
                    HorizontalDivider(color = dividerColor, modifier = Modifier.padding(start = 72.dp))
                    SettingsClickItem(
                        painter = painterResource(id = R.drawable.cloud_file_download_icon),
                        title = "Restore Data",
                        subtitle = state.lastRestoreTime ?: "Restore tasks from cloud backup",
                        isLoading = state.isRestoreLoading,
                        iconBgColor = if (isDarkMode) Color(0xFF1E3D3D) else Color(0xFFE7F6F2),
                        iconTintColor = Color(0xFF0D9488),
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        brandColor = brandColor,
                        onClick = { onEvent(SettingsEvent.RestoreData) }
                    )
                }
            }

            // ─── Account Section ──────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SettingsSectionHeader("ACCOUNT", secondaryText)
                SettingsGroupCard(cardColor, dividerColor) {
                    SettingsClickItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        title = "Sign Out",
                        subtitle = "Logout from your account",
                        isLoading = false,
                        iconBgColor = if (isDarkMode) Color(0xFF3D1E1E) else Color(0xFFFFEBEB),
                        iconTintColor = Color(0xFFDC2626),
                        primaryText = primaryText,
                        secondaryText = secondaryText,
                        brandColor = brandColor,
                        onClick = { onEvent(SettingsEvent.SignOut) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding() + 40.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(text: String, color: Color) {
    Text(modifier = Modifier.offset(y = 5.dp),
        text = text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
    )
}

@Composable
fun SettingsGroupCard(cardColor: Color, dividerColor: Color, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = cardColor,
        border = BorderStroke(1.dp, dividerColor),
        content = { Column(content = content) }
    )
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    iconBgColor: Color,
    iconTintColor: Color,
    primaryText: Color,
    secondaryText: Color,
    brandColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTintColor,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = primaryText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = secondaryText, fontSize = 12.sp)
        }
        // Custom toggle icon
        Icon(
            painter = painterResource(
                id = if (isChecked) R.drawable.toggle_on_line_icon
                     else R.drawable.toggle_off_line_icon
            ),
            contentDescription = "Toggle Theme",
            tint = if (isChecked) brandColor else secondaryText.copy(alpha = 0.4f),
            modifier = Modifier
                .offset(y = (-8).dp)
                .size(width = 44.dp, height = 24.dp)
        )
    }
}

@Composable
fun SettingsClickItem(
    icon: ImageVector? = null,
    painter: Painter? = null,
    title: String,
    subtitle: String,
    isLoading: Boolean,
    iconBgColor: Color,
    iconTintColor: Color,
    primaryText: Color,
    secondaryText: Color,
    brandColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Box - Always shows the icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(painter = painterResource(id = R.drawable.logout_icon),
                    contentDescription = null,
                    tint = iconTintColor,
                    modifier = Modifier.size(20.dp))
            } else if (painter != null) {
                Icon(painter = painter, contentDescription = null, tint = iconTintColor, modifier = Modifier.size(20.dp))
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = primaryText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(subtitle, color = if (subtitle.startsWith("Last")) brandColor else secondaryText, fontSize = 12.sp)
        }
        
        // Loading Indicator or Arrow at the end (Right side)
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = brandColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    TodoAppTheme {
        SettingsScreen(
            state = SettingsState(),
            isDarkMode = false,
            onToggleDarkMode = {},
            onEvent = {},
            onNavigateBack = {},
            onSignedOut = {}
        )
    }
}
