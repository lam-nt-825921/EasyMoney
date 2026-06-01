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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
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
                    onClick = { viewModel.submitApplication(loanData, onConfirmed) },
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(
                            if (uiState.isSubmitting) R.string.confirm_loan_submitting
                            else R.string.confirm_loan_submit
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
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
            ReviewSection(title = stringResource(R.string.confirm_loan_section_loan_info)) {
                ReviewRow(
                    stringResource(R.string.confirm_loan_row_lender),
                    stringResource(R.string.confirm_loan_row_lender_name)
                )
                ReviewRow(stringResource(R.string.confirm_loan_row_amount), formatCurrency(loanData?.loanAmount ?: 0L))
                ReviewRow(
                    stringResource(R.string.confirm_loan_row_tenor),
                    stringResource(R.string.loan_tenor_value_months, loanData?.tenorMonth ?: 0)
                )
                ReviewRow(
                    stringResource(R.string.confirm_loan_row_insurance),
                    if (loanData?.hasInsurance == true) stringResource(R.string.confirm_loan_value_insurance_yes)
                    else stringResource(R.string.confirm_loan_value_insurance_no)
                )
                loanData?.voucherId?.let { ReviewRow(stringResource(R.string.confirm_loan_row_voucher), it) }
                ReviewRow(
                    stringResource(R.string.confirm_loan_row_loan_method),
                    stringResource(R.string.confirm_loan_value_loan_method)
                )
                ReviewRow(
                    stringResource(R.string.confirm_loan_row_repay_method),
                    stringResource(R.string.confirm_loan_value_repay_method)
                )
            }

            // Section: Tài khoản nhận giải ngân (Thông tin này thường fix cứng theo TK đăng ký hoặc lấy từ form)
            ReviewSection(title = stringResource(R.string.confirm_loan_section_disbursement)) {
                ReviewRow(stringResource(R.string.confirm_loan_row_account_holder), "NGUYEN DUC MINH")
                ReviewRow(stringResource(R.string.confirm_loan_row_bank), "TPBANK")
                ReviewRow(stringResource(R.string.confirm_loan_row_account_number), "0936552900")
            }

            // Section: Địa chỉ thường trú
            ReviewSection(title = stringResource(R.string.confirm_loan_section_permanent_address)) {
                ReviewRow(
                    stringResource(R.string.confirm_loan_row_administrative),
                    "${loanData?.permanentWard ?: ""}, ${loanData?.permanentDistrict ?: ""}, ${loanData?.permanentProvince ?: ""}".trim(',', ' ')
                )
                ReviewRow(stringResource(R.string.confirm_loan_row_address_detail), loanData?.permanentDetail ?: "")
            }

            // Section: Địa chỉ hiện tại
            ReviewSection(title = stringResource(R.string.confirm_loan_section_current_address)) {
                ReviewRow(
                    stringResource(R.string.confirm_loan_row_administrative),
                    "${loanData?.currentWard ?: ""}, ${loanData?.currentDistrict ?: ""}, ${loanData?.currentProvince ?: ""}".trim(',', ' ')
                )
                ReviewRow(stringResource(R.string.confirm_loan_row_address_detail), loanData?.currentDetail ?: "")
            }

            // Section: Thông tin cá nhân
            ReviewSection(title = stringResource(R.string.confirm_loan_section_personal_info)) {
                ReviewRow(stringResource(R.string.confirm_loan_row_income), formatCurrency(loanData?.monthlyIncome ?: 0L))
                ReviewRow(stringResource(R.string.confirm_loan_row_profession), loanData?.profession ?: "")
                ReviewRow(stringResource(R.string.confirm_loan_row_position), loanData?.position ?: "")
                ReviewRow(stringResource(R.string.confirm_loan_row_education), loanData?.education ?: "")
                ReviewRow(stringResource(R.string.confirm_loan_row_marital), loanData?.maritalStatus ?: "")
            }

            // Section: Thông tin người liên hệ
            ReviewSection(title = stringResource(R.string.confirm_loan_section_contact_person)) {
                ReviewRow(stringResource(R.string.confirm_loan_row_contact_name), loanData?.contactName ?: "")
                ReviewRow(stringResource(R.string.confirm_loan_row_contact_relationship), loanData?.contactRelationship ?: "")
                ReviewRow(stringResource(R.string.confirm_loan_row_contact_phone), loanData?.contactPhone ?: "")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    uiState.error?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearError,
            title = { Text(stringResource(R.string.confirm_loan_error_title)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::clearError) {
                    Text(stringResource(R.string.confirm_loan_error_close))
                }
            }
        )
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
