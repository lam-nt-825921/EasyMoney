package com.example.easymoney.ui.loan.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoanStepper(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = Color(0xFFD0D5DD)
    val itemWidth = 100.dp

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = itemWidth / 2, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = if (currentStep > 1) activeColor else inactiveColor,
                thickness = 2.dp
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = if (currentStep > 2) activeColor else inactiveColor,
                thickness = 2.dp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StepItem(1, "Chọn khoản vay", currentStep >= 1, itemWidth, activeColor, inactiveColor)
            StepItem(2, "Điền thông tin", currentStep >= 2, itemWidth, activeColor, inactiveColor)
            StepItem(3, "Xác nhận", currentStep >= 3, itemWidth, activeColor, inactiveColor)
        }
    }
}

@Composable
private fun StepItem(
    step: Int,
    label: String,
    isActive: Boolean,
    width: Dp,
    activeColor: Color,
    inactiveColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(width)
    ) {
        Surface(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            shape = RoundedCornerShape(16.dp),
            color = if (isActive) activeColor else inactiveColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(step.toString(), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = if (isActive) activeColor else Color(0xFF667085),
            lineHeight = 14.sp
        )
    }
}


