package com.example.easymoney.ui.loan.discovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.ui.theme.TextPrimary
import com.example.easymoney.ui.theme.TextSecondary
import com.example.easymoney.ui.home.EligibilityUiState
import com.example.easymoney.R

@Composable
fun LoanDetailScreen(
    packageId: String,
    onBack: () -> Unit,
    onRegisterSuccess: (String, String) -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: LoanDiscoveryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(packageId) {
        viewModel.loadPackageDetail(packageId)
    }

    // Handle Eligibility results
    LaunchedEffect(uiState.eligibilityState) {
        when (val state = uiState.eligibilityState) {
            is EligibilityUiState.Success -> {
                onRegisterSuccess(state.packageId, uiState.selectedPackage?.packageName ?: "")
                viewModel.resetEligibilityState()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "Lỗi tải dữ liệu",
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.error
            )
        } else {
            uiState.selectedPackage?.let { loanPackage ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = loanPackage.packageName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Dành cho khách hàng có điểm tín dụng từ ${loanPackage.eligibleCreditScore}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }

                    // Details Card
                    DetailSection(title = "Thông tin chi tiết") {
                        DetailItem(label = "Hạn mức vay", value = "%,d - %,dđ".format(loanPackage.minAmount, loanPackage.maxAmount).replace(',', '.'))
                        DetailItem(label = "Thời hạn vay", value = "${loanPackage.tenorRange} tháng")
                        DetailItem(label = "Lãi suất ưu đãi", value = "${loanPackage.interest}%/năm")
                        DetailItem(label = "Phí quá hạn", value = "${loanPackage.overdueCost}%/ngày")
                    }

                    // Conditions Card
                    DetailSection(title = "Điều kiện đăng ký") {
                        ConditionItem(text = "Công dân Việt Nam từ 20 - 60 tuổi")
                        ConditionItem(text = "Có thu nhập ổn định từ 5 triệu đồng/tháng")
                        ConditionItem(text = "Có lịch sử tín dụng tốt (không nợ xấu)")
                    }

                    // Illustration Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE0F2F1),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = TealPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Ví dụ: Vay 10tr trong 6 tháng với lãi suất 12%/năm, mỗi tháng bạn trả khoảng 1.725.000đ.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TealPrimary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(80.dp))
                }

                // Bottom Action
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Button(
                        onClick = { viewModel.checkEligibility(packageId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Text(stringResource(id = R.string.loan_detail_register_now), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (uiState.eligibilityState is EligibilityUiState.Checking) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    // Eligibility Dialogs
    if (uiState.eligibilityState is EligibilityUiState.MissingInfo) {
        val state = uiState.eligibilityState as EligibilityUiState.MissingInfo
        AlertDialog(
            onDismissRequest = { viewModel.resetEligibilityState() },
            title = { Text(stringResource(id = R.string.dialog_incomplete_profile_title)) },
            text = { Text(state.message) },
            confirmButton = {
                Button(onClick = { 
                    viewModel.resetEligibilityState()
                    onNavigateToProfile() 
                }) {
                    Text(stringResource(id = R.string.dialog_incomplete_profile_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resetEligibilityState() }) {
                    Text(stringResource(id = R.string.dialog_button_close))
                }
            }
        )
    }

    if (uiState.eligibilityState is EligibilityUiState.Rejected) {
        val state = uiState.eligibilityState as EligibilityUiState.Rejected
        AlertDialog(
            onDismissRequest = { viewModel.resetEligibilityState() },
            title = { Text(stringResource(id = R.string.dialog_ineligible_title)) },
            text = { Text(state.message) },
            confirmButton = {
                Button(onClick = { viewModel.resetEligibilityState() }) {
                    Text(stringResource(id = R.string.dialog_button_understand))
                }
            }
        )
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun ConditionItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(TealPrimary, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

