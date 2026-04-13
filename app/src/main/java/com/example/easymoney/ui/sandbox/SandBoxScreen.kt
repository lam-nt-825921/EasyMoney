package com.example.easymoney.ui.sandbox

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SandBoxScreen(
    onBack: () -> Unit,
    onEsign: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SandBoxViewModel = hiltViewModel()
) {
    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onEsign,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
            ) {
                Text(text = "Ký hợp đồng")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.simulateTransactionNotification() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
            ) {
                Text(text = "Mô phỏng Nhận tiền (+5tr)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.simulateLoanNotification() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
            ) {
                Text(text = "Mô phỏng Nhắc nợ")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.simulatePromotionNotification() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(56.dp)
            ) {
                Text(text = "Mô phỏng Khuyến mãi")
            }
        }
    }
}
