package com.todoapp.presentation.screens.tasklist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.todoapp.R
import com.todoapp.domain.model.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    selectedSortOrder: SortOrder,
    isDarkMode: Boolean,
    onSortOrderSelected: (SortOrder) -> Unit,
    onDismiss: () -> Unit,
    onReset: () -> Unit
) {
    val containerColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
    val resetBg = if (isDarkMode) Color(0xFF2D2D4D) else Color(0xFFF0EEFF)
    val cancelBtnBg = if (isDarkMode) Color(0xFF2C2C2C) else Color(0xFFF1F1F1)
    val cancelBtnText = if (isDarkMode) Color(0xFFAAAAAA) else Color(0xFF666666)
    
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 14.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(if (isDarkMode) Color(0xFF333333) else Color(0xFFE2E8F0))
            )
        },
        containerColor = containerColor,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // Professional bottom spacing
                .padding(horizontal = 24.dp)
                .padding(bottom = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.sort_by),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        fontSize = 22.sp
                    )
                )
                TextButton(
                    onClick = onReset,
                    colors = ButtonDefaults.textButtonColors(containerColor = resetBg),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.reset),
                        color = Color(0xFF7B61FF),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Sort Options - Use LazyColumn with fixed constraints to prevent "dancing"
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp), // Fixed max height for stability
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(SortOrder.entries) { order ->
                    SortOptionItem(
                        order = order,
                        isSelected = order == selectedSortOrder,
                        isDarkMode = isDarkMode,
                        onClick = { onSortOrderSelected(order) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cancelBtnBg,
                        contentColor = cancelBtnText
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B61FF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp,
                        hoveredElevation = 7.dp,
                        focusedElevation = 7.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.apply_sort),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SortOptionItem(
    order: SortOrder,
    isSelected: Boolean,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    val unselectedBg = if (isDarkMode) Color(0xFF262626) else Color(0xFFF8F8F8)
    val backgroundColor = if (isSelected) Color(0xFF7B61FF) else unselectedBg
    val contentColor = if (isSelected) Color.White else (if (isDarkMode) Color.White else Color(0xFF1A1A1A))
    val subTextColor = if (isSelected) Color.White.copy(alpha = 0.7f) else (if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF999999))
    
    val iconInfo = getSortIconInfo(order, isDarkMode)

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) Color.White.copy(alpha = 0.2f) else iconInfo.bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconInfo.iconRes),
                    contentDescription = null,
                    tint = if (isSelected) Color.White else iconInfo.tint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = order.label,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
                Text(
                    text = stringResource(getSortDescriptionRes(order)),
                    fontSize = 12.sp,
                    color = subTextColor
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

private data class SortIconInfo(val iconRes: Int, val tint: Color, val bg: Color)

@Composable
private fun getSortIconInfo(order: SortOrder, isDarkMode: Boolean): SortIconInfo {
    return when (order) {
        SortOrder.DATE_CREATED_ASC -> SortIconInfo(
            iconRes = R.drawable.old_board_icon, 
            tint = Color(0xFFC68A39), 
            bg = if (isDarkMode) Color(0xFF3B2A1A) else Color(0xFFFFF4E5)
        )
        SortOrder.DATE_CREATED_DESC -> SortIconInfo(
            iconRes = R.drawable.new_board_icon, 
            tint = Color(0xFF7B61FF),
            bg = if (isDarkMode) Color(0xFF2D2D4D) else Color(0xFFE5E5E5)
        )
        SortOrder.DUE_DATE_ASC -> SortIconInfo(
            R.drawable.date_line, 
            Color(0xFF00A67E), 
            if (isDarkMode) Color(0xFF1E3D3D) else Color(0xFFE5F6F2)
        )
        SortOrder.DUE_DATE_DESC -> SortIconInfo(
            R.drawable.date_line, 
            Color(0xFF3F8CFF), 
            if (isDarkMode) Color(0xFF1E2D4D) else Color(0xFFEBF3FF)
        )
        SortOrder.PRIORITY_HIGH_FIRST -> SortIconInfo(
            R.drawable.increase_up_profit_icon, 
            Color(0xFFFF6B6B), 
            if (isDarkMode) Color(0xFF3D1E1E) else Color(0xFFFFF0F0)
        )
        SortOrder.PRIORITY_LOW_FIRST -> SortIconInfo(
            R.drawable.decrease_down_loss_icon, 
            Color(0xFF00A67E), 
            if (isDarkMode) Color(0xFF1E3D3D) else Color(0xFFE5F6F2)
        )
        SortOrder.ALPHABETICAL -> SortIconInfo(
            R.drawable.filter, 
            Color(0xFF999999), 
            if (isDarkMode) Color(0xFF333333) else Color(0xFFF1F1F1)
        )
        SortOrder.AI_SMART -> SortIconInfo(
            R.drawable.star, 
            Color(0xFF7B61FF), 
            if (isDarkMode) Color(0xFF2D2D4D) else Color(0xFFF0EEFF)
        )
    }
}

private fun getSortDescriptionRes(order: SortOrder): Int {
    return when (order) {
        SortOrder.DATE_CREATED_ASC -> R.string.created_asc
        SortOrder.DATE_CREATED_DESC -> R.string.created_desc
        SortOrder.DUE_DATE_ASC -> R.string.urgent_first
        SortOrder.DUE_DATE_DESC -> R.string.latest_first
        SortOrder.PRIORITY_HIGH_FIRST -> R.string.critical_first
        SortOrder.PRIORITY_LOW_FIRST -> R.string.low_priority_first
        SortOrder.ALPHABETICAL -> R.string.alphabetical
        SortOrder.AI_SMART -> R.string.ai_smart_order
    }
}
