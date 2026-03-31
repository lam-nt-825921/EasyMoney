package com.example.easymoney.ui.loan.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoanBottomButton(
    modifier: Modifier = Modifier,
    isInsuranceSelected: Boolean,
    onInsuranceToggled: (Boolean) -> Unit,
    onNextClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Checkbox(
                checked = isInsuranceSelected,
                onCheckedChange = { onInsuranceToggled(it) },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = "Bảo hiểm người vay",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp)
            )
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = Color.Red.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(18.dp)
            )
        }
        Button(
            onClick = onNextClick,
            enabled = isInsuranceSelected,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            )
        ) {
            Text("Tiếp tục", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
