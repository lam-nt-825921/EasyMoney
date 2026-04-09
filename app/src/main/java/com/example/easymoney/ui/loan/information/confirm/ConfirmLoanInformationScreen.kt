package com.example.easymoney.ui.loan.information.confirm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.domain.model.LoanApplicationRequest
import com.example.easymoney.ui.loan.formatCurrency

@Composable
fun ConfirmLoanInformationScreen(
    loanData: LoanApplicationRequest?,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfirmLoanInformationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = onConfirmed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = "Xác nhận", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section: Thông tin khoản vay
            ReviewSection(title = "Thông tin khoản vay") {
                ReviewRow("Số tiền vay mong muốn", formatCurrency(loanData?.loanAmount ?: 0L))
                ReviewRow("Kỳ hạn vay", "${loanData?.tenorMonth ?: 0} tháng")
                ReviewRow("Bảo hiểm khoản vay", if (loanData?.hasInsurance == true) "Có tham gia" else "Không tham gia")
                ReviewRow("Phương thức cho vay", "Cho vay từng lần")
                ReviewRow("Phương thức trả nợ", "Trả gốc và lãi hàng tháng")
            }

            // Section: Tài khoản nhận giải ngân (Thông tin này thường fix cứng theo TK đăng ký hoặc lấy từ form)
            ReviewSection(title = "Tài khoản nhận giải ngân") {
                ReviewRow("Chủ tài khoản", "NGUYEN DUC MINH")
                ReviewRow("Ngân hàng", "TPBANK")
                ReviewRow("Số tài khoản", "0936552900")
            }

            // Section: Địa chỉ thường trú
            ReviewSection(title = "Địa chỉ thường trú") {
                ReviewRow(
                    "Tỉnh/Thành phố, Quận/Huyện, Phường/Xã", 
                    "${loanData?.permanentWard ?: ""}, ${loanData?.permanentDistrict ?: ""}, ${loanData?.permanentProvince ?: ""}".trim(',', ' ')
                )
                ReviewRow("Địa chỉ chi tiết", loanData?.permanentDetail ?: "")
            }

            // Section: Địa chỉ hiện tại
            ReviewSection(title = "Địa chỉ hiện tại") {
                ReviewRow(
                    "Tỉnh/Thành phố, Quận/Huyện, Phường/Xã", 
                    "${loanData?.currentWard ?: ""}, ${loanData?.currentDistrict ?: ""}, ${loanData?.currentProvince ?: ""}".trim(',', ' ')
                )
                ReviewRow("Địa chỉ chi tiết", loanData?.currentDetail ?: "")
            }

            // Section: Thông tin cá nhân
            ReviewSection(title = "Thông tin cá nhân") {
                ReviewRow("Thu nhập hàng tháng", formatCurrency(loanData?.monthlyIncome ?: 0L))
                ReviewRow("Nghề nghiệp", loanData?.profession ?: "")
                ReviewRow("Chức vụ", loanData?.position ?: "")
                ReviewRow("Trình độ học vấn", loanData?.education ?: "")
                ReviewRow("Tình trạng hôn nhân", loanData?.maritalStatus ?: "")
            }

            // Section: Thông tin người liên hệ
            ReviewSection(title = "Thông tin người liên hệ") {
                ReviewRow("Họ và tên", loanData?.contactName ?: "")
                ReviewRow("Mối quan hệ với bạn", loanData?.contactRelationship ?: "")
                ReviewRow("Số điện thoại", loanData?.contactPhone ?: "")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ReviewSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title, 
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold, 
            color = MaterialTheme.colorScheme.onBackground, 
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            modifier = Modifier.weight(1.2f)
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface, 
            fontWeight = FontWeight.Medium, 
            modifier = Modifier.weight(1f), 
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}
