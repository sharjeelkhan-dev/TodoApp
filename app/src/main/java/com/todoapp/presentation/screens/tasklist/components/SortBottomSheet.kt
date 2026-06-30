package com.todoapp.presentation.screens.tasklist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.todoapp.R
import com.todoapp.domain.model.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    selectedSortOrder: SortOrder,
    onSortOrderSelected: (SortOrder) -> Unit,
    onDismiss: () -> Unit,
    onReset: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.4f))
            )
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.sort_by),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Surface(
                    onClick = onReset,
                    color = Color(0xFFF0EEFF),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.reset),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = Color(0xFF7B61FF),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }

            // Sort Options
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f, fill = false)
            ) {
                items(SortOrder.entries) { order ->
                    SortOptionItem(
                        order = order,
                        isSelected = order == selectedSortOrder,
                        onClick = { onSortOrderSelected(order) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1F1F1),
                        contentColor = Color(0xFF666666)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B61FF),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.apply_sort),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
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
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFF7B61FF) else Color(0xFFF8F8F8)
    val contentColor = if (isSelected) Color.White else Color(0xFF1A1A1A)
    val subTextColor = if (isSelected) Color.White.copy(alpha = 0.7f) else Color(0xFF999999)
    
    val iconInfo = getSortIconInfo(order)

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
private fun getSortIconInfo(order: SortOrder): SortIconInfo {
    return when (order) {
        SortOrder.DATE_CREATED_ASC -> SortIconInfo(
            iconRes = R.drawable.old_board_icon, 
            tint = Color(0xFFC68A39), 
            bg = Color(0xFFFFF4E5)
        )
        SortOrder.DATE_CREATED_DESC -> SortIconInfo(
            iconRes = R.drawable.new_board_icon, 
            tint = Color(0xFF7B61FF),
            bg = Color(0xFFE5E5E5)
        )
        SortOrder.DUE_DATE_ASC -> SortIconInfo(R.drawable.date_line, Color(0xFF00A67E), Color(0xFFE5F6F2))
        SortOrder.DUE_DATE_DESC -> SortIconInfo(R.drawable.date_line, Color(0xFF3F8CFF), Color(0xFFEBF3FF))
        SortOrder.PRIORITY_HIGH_FIRST -> SortIconInfo(R.drawable.increase_up_profit_icon, Color(0xFFFF6B6B), Color(0xFFFFF0F0))
        SortOrder.PRIORITY_LOW_FIRST -> SortIconInfo(R.drawable.decrease_down_loss_icon, Color(0xFF00A67E), Color(0xFFE5F6F2))
        SortOrder.ALPHABETICAL -> SortIconInfo(R.drawable.filter, Color(0xFF999999), Color(0xFFF1F1F1))
        SortOrder.AI_SMART -> SortIconInfo(R.drawable.star, Color(0xFF7B61FF), Color(0xFFF0EEFF))
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
