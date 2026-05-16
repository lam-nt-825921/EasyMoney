package com.example.easymoney.ui.loan.flow

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.easymoney.ui.theme.EasyMoneyTheme

// Preview chỉ giữ chỗ — LoanFlowViewModel cần Hilt DI, không khởi tạo trực tiếp được.
// Xem màn hình thật khi chạy app để kiểm tra UI.
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoanFlowPreview() {
    EasyMoneyTheme {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("LoanFlow preview unavailable — chạy app để test")
        }
    }
}
