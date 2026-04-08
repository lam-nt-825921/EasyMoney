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
import com.example.easymoney.ui.loan.formatCurrency
import com.example.easymoney.ui.loan.information.form.LoanInformationFormUiState

@Composable
fun ConfirmLoanInformationScreen(
    formData: LoanInformationFormUiState,
    onConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfirmLoanInformationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(formData) {
        viewModel.setFormData(formData)
    }

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
                    onClick = { viewModel.onConfirmClick(onConfirmed) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(text = "Xác nhận", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
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
                ReviewRow("Số tiền vay mong muốn", "10.000.000đ")
                ReviewRow("Kỳ hạn vay", "6 tháng")
                ReviewRow("Phương thức cho vay", "Cho vay từng lần")
                ReviewRow("Phương thức trả nợ", "Trả gốc và lãi hàng tháng")
                ReviewRow("Mục đích vay", "Mua đồ dùng cá nhân, trang thiết bị gia đình")
            }

            // Section: Tài khoản nhận giải ngân
            ReviewSection(title = "Tài khoản nhận giải ngân") {
                ReviewRow("Ngân hàng", formData.bankName)
                ReviewRow("Số tài khoản", formData.accountNumber)
                ReviewRow("Chủ tài khoản", formData.accountOwner)
            }

            // Section: Địa chỉ thường trú
            ReviewSection(title = "Địa chỉ thường trú") {
                ReviewRow(
                    "Tỉnh/Thành phố, Quận/Huyện, Phường/Xã", 
                    "${formData.permanentWard?.name}, ${formData.permanentDistrict?.name}, ${formData.permanentProvince?.name}"
                )
                ReviewRow("Địa chỉ chi tiết", formData.permanentDetail)
            }

            // Section: Địa chỉ hiện tại
            ReviewSection(title = "Địa chỉ hiện tại") {
                val province = if (formData.isCurrentSameAsPermanent) formData.permanentProvince?.name else formData.currentProvince?.name
                val district = if (formData.isCurrentSameAsPermanent) formData.permanentDistrict?.name else formData.currentDistrict?.name
                val ward = if (formData.isCurrentSameAsPermanent) formData.permanentWard?.name else formData.currentWard?.name
                val detail = if (formData.isCurrentSameAsPermanent) formData.permanentDetail else formData.currentDetail
                
                ReviewRow("Tỉnh/Thành phố, Quận/Huyện, Phường/Xã", "${ward ?: ""}, ${district ?: ""}, ${province ?: ""}".trim(',', ' '))
                ReviewRow("Địa chỉ chi tiết", detail)
            }

            // Section: Thông tin cá nhân
            ReviewSection(title = "Thông tin cá nhân") {
                ReviewRow("Thu nhập hàng tháng", formatCurrency(formData.monthlyIncome.toLongOrNull() ?: 0L))
                ReviewRow("Nghề nghiệp", formData.profession?.name ?: "")
                if (formData.profession?.id == "p1") {
                    ReviewRow("Tên công ty", formData.companyName)
                    ReviewRow("Chức vụ", formData.position?.name ?: "")
                }
                ReviewRow("Trình độ học vấn", formData.education?.name ?: "")
                ReviewRow("Tình trạng hôn nhân", formData.maritalStatus?.name ?: "")
            }

            // Section: Thông tin vợ/chồng
            if (formData.maritalStatus?.id == "m2") {
                ReviewSection(title = "Thông tin vợ/chồng") {
                    ReviewRow("Họ và tên", formData.spouseName)
                    ReviewRow("Số điện thoại", formData.spousePhone)
                }
            }

            // Section: Thông tin người liên hệ
            ReviewSection(title = "Thông tin người liên hệ") {
                ReviewRow("Họ và tên", formData.contactName)
                ReviewRow("Mối quan hệ với bạn", formData.contactRelationship?.name ?: "")
                ReviewRow("Số điện thoại", formData.contactPhone)
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
