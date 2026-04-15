package com.example.easymoney.ui.sandbox

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.data.local.DataSourceMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SandBoxScreen(
    onBack: () -> Unit,
    onEsign: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SandBoxViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Xử lý Close App Effect
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is SandboxEffect.CloseApp -> {
                    (context as? Activity)?.finish()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer Sandbox") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Cấu hình kết nối ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cấu hình Kết nối", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Nguồn dữ liệu (DataSource Mode):", fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.dataSourceMode == DataSourceMode.MOCK,
                            onClick = { viewModel.toggleDataSourceMode(DataSourceMode.MOCK) },
                            label = { Text("MOCK (Local)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = uiState.dataSourceMode == DataSourceMode.REMOTE,
                            onClick = { viewModel.toggleDataSourceMode(DataSourceMode.REMOTE) },
                            label = { Text("REMOTE (API)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("API Base URL (LAN):", fontSize = 14.sp)
                    OutlinedTextField(
                        value = uiState.apiBaseUrl,
                        onValueChange = { viewModel.updateApiBaseUrl(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("http://192.168.1.x:8000") },
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- NÚT TEST FCM (CHỈ HIỆN Ở MODE REMOTE) ---
            if (uiState.dataSourceMode == DataSourceMode.REMOTE) {
                val buttonText = if (uiState.countdown != null) {
                    "Đóng app sau ${uiState.countdown}s..."
                } else if (uiState.isTriggering) {
                    "Đang gọi API..."
                } else {
                    "Test FCM (Thật): Gọi API & Tắt App"
                }

                Button(
                    onClick = { viewModel.startFcmTestFlow() },
                    enabled = !uiState.isTriggering,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(buttonText, fontWeight = FontWeight.Bold)
                }
                
                Text(
                    "Luồng: Click -> Gọi Backend -> Đếm ngược 3s -> Tắt App hoàn toàn. Sau 5s Backend sẽ gửi thông báo tới máy này.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )
            }

            // --- Điều hướng & Tính năng ---
            Text("Thử nghiệm Tính năng", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onEsign,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Màn hình Ký hợp đồng")
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Mô phỏng Thông báo ---
            Text("Mô phỏng Thông báo (Local)", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.simulateTransactionNotification() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Nhận tiền (+5.000.000đ)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { viewModel.simulateLoanNotification() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Nhắc nợ Khoản vay")
            }
        }
    }
}
