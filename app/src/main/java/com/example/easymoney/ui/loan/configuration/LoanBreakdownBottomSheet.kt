package com.example.easymoney.ui.loan.configuration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easymoney.ui.loan.configuration.LoanConfigurationUiState
import com.example.easymoney.ui.loan.formatCurrency

@Composable
fun LoanBreakdownBottomSheet(state: LoanConfigurationUiState, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Tổng tiền tạm tính",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp
        )
        HorizontalDivider(color = Color(0xFFEAECF0))

        Column(modifier = Modifier.padding(20.dp)) {
            SummaryRow(label = "Số tiền thực nhận", value = formatCurrency(state.actualReceivedAmount))
            if (state.insuranceFee > 0) {
                SummaryRow(label = "Phí bảo hiểm", value = formatCurrency(state.insuranceFee))
            }
            SummaryRow(label = "Tiền lãi", value = formatCurrency(state.interestAmount))

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 14.dp),
                thickness = 1.dp,
                color = Color(0xFFEAECF0)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Tổng tiền phải trả\n(tạm tính)",
                    color = Color(0xFF667085),
                    fontSize = 16.sp,
                    lineHeight = 20.sp
                )
                Text(
                    text = formatCurrency(state.totalPayment),
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF667085), fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 16.sp)
    }
}
