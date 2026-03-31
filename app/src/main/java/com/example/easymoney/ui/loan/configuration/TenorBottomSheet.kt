package com.example.easymoney.ui.loan.configuration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TenorBottomSheetContent(
    tenors: List<Int>,
    selectedTenor: Int,
    onTenorSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Kỳ hạn vay",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        HorizontalDivider()
        
        tenors.forEach { tenor ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTenorSelected(tenor) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("$tenor tháng", fontSize = 16.sp)
                if (tenor == selectedTenor) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    RadioButton(
                        selected = false,
                        onClick = { onTenorSelected(tenor) },
                        colors = RadioButtonDefaults.colors(unselectedColor = Color(0xFFB3B3B3))
                    )
                }
            }
            HorizontalDivider(color = Color(0xFFEAECF0))
        }
    }
}
