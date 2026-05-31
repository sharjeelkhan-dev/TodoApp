package com.todoapp.presentation.theme

import androidx.compose.ui.graphics.Color
import com.todoapp.domain.model.TaskCategory
import com.todoapp.domain.model.TaskPriority

// ─── Primary Palette (Indigo-based) ──────────────────────────────
val PrimaryLight = Color(0xFF4F46E5)
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = Color(0xFFE0E7FF)
val OnPrimaryContainerLight = Color(0xFF1E1B4B)

val PrimaryDark = Color(0xFFA5B4FC)
val OnPrimaryDark = Color(0xFF1E1B4B)
val PrimaryContainerDark = Color(0xFF3730A3)
val OnPrimaryContainerDark = Color(0xFFE0E7FF)

// ─── Secondary Palette (Teal) ──────────────────────────────
val SecondaryLight = Color(0xFF0D9488)
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = Color(0xFFCCFBF1)
val OnSecondaryContainerLight = Color(0xFF134E4A)

val SecondaryDark = Color(0xFF5EEAD4)
val OnSecondaryDark = Color(0xFF134E4A)
val SecondaryContainerDark = Color(0xFF115E59)
val OnSecondaryContainerDark = Color(0xFFCCFBF1)

// ─── Tertiary Palette (Amber) ──────────────────────────────
val TertiaryLight = Color(0xFFD97706)
val OnTertiaryLight = Color(0xFFFFFFFF)
val TertiaryContainerLight = Color(0xFFFEF3C7)
val OnTertiaryContainerLight = Color(0xFF78350F)

val TertiaryDark = Color(0xFFFBBF24)
val OnTertiaryDark = Color(0xFF78350F)
val TertiaryContainerDark = Color(0xFF92400E)
val OnTertiaryContainerDark = Color(0xFFFEF3C7)

// ─── Background & Surface ──────────────────────────────
val BackgroundLight = Color(0xFFF8FAFC)
val OnBackgroundLight = Color(0xFF0F172A)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF0F172A)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnSurfaceVariantLight = Color(0xFF475569)

val BackgroundDark = Color(0xFF0F172A)
val OnBackgroundDark = Color(0xFFE2E8F0)
val SurfaceDark = Color(0xFF1E293B)
val OnSurfaceDark = Color(0xFFE2E8F0)
val SurfaceVariantDark = Color(0xFF334155)
val OnSurfaceVariantDark = Color(0xFFCBD5E1)

// ─── Error ──────────────────────────────
val ErrorLight = Color(0xFFDC2626)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFEE2E2)
val OnErrorContainerLight = Color(0xFF7F1D1D)

val ErrorDark = Color(0xFFFCA5A5)
val OnErrorDark = Color(0xFF7F1D1D)
val ErrorContainerDark = Color(0xFF991B1B)
val OnErrorContainerDark = Color(0xFFFEE2E2)

// ─── Priority Colors ──────────────────────────────
val PriorityHigh = Color(0xFFEF4444)
val PriorityMedium = Color(0xFFF59E0B)
val PriorityLow = Color(0xFF22C55E)

// ─── Category Colors ──────────────────────────────
val CategoryWork = Color(0xFF7B61FF)
val CategoryPersonal = Color(0xFFBA68C8)
val CategoryStudy = Color(0xFF64B5F6)
val CategoryHealth = Color(0xFF81C784)
val CategoryShopping = Color(0xFFFFD54F)
val CategoryFinance = Color(0xFF4CAF50)
val CategoryHome = Color(0xFFFF7043)
val CategoryOther = Color(0xFF90A4AE)

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

fun getPriorityColor(priority: TaskPriority): Color = when (priority) {
    TaskPriority.HIGH -> PriorityHigh
    TaskPriority.MEDIUM -> PriorityMedium
    TaskPriority.LOW -> PriorityLow
}
