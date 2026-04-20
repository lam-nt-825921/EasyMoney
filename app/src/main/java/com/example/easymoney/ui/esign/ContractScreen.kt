package com.example.easymoney.ui.esign

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.domain.repository.LoanRepositoryImpl
import com.example.easymoney.ui.common.components.OtpDialog
import com.example.easymoney.ui.common.loading.SkeletonBlock
import com.example.easymoney.ui.theme.EasyMoneyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractScreen(
    loanId: String,
    onSignSuccess: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ContractViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Load contract content when the screen starts or loanId changes
    LaunchedEffect(loanId) {
        viewModel.loadContract(loanId)
    }

    if (uiState.showOtpDialog) {
        OtpDialog(
            phoneNumber = uiState.userPhone,
            onDismiss = viewModel::hideOtpDialog,
            onConfirm = { otp -> viewModel.verifyOtp(otp, onSignSuccess) },
            onResendOtp = viewModel::resendOtp,
            onMaxAttemptsReached = {
                viewModel.hideOtpDialog()
                onCancel()
            },
            isVerifying = uiState.isOtpVerifying,
            errorMessage = uiState.otpError
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            ContractBottomSection(
                isAccepted = uiState.isTermsAccepted,
                isSigning = uiState.isSigning,
                onAcceptedChange = viewModel::onTermsAcceptedChange,
                onSignClick = { viewModel.signContract(onSignSuccess) },
                onCancelClick = onCancel
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (uiState.isLoading) {
                ContractLoadingContent()
            } else {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = uiState.contractContent,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContractLoadingContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SkeletonBlock(height = 40.dp, modifier = Modifier.fillMaxWidth(0.7f))
        repeat(10) {
            SkeletonBlock(height = 20.dp, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ContractBottomSection(
    isAccepted: Boolean,
    isSigning: Boolean,
    onAcceptedChange: (Boolean) -> Unit,
    onSignClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Checkbox and Agreement Text
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isAccepted,
                    onCheckedChange = onAcceptedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary
                    )
                )
                
                val annotatedString = buildAnnotatedString {
                    append("Tôi đã đọc và đồng ý với ")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)) {
                        append("Điều khoản sử dụng")
                    }
                }
                
                Text(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Sign Button
            Button(
                onClick = onSignClick,
                enabled = isAccepted && !isSigning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(27.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                if (isSigning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Ký hợp đồng",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Cancel Button
            TextButton(
                onClick = onCancelClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Hủy hợp đồng",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Contract Screen Light")
@Composable
private fun ContractScreenPreview() {
    val viewModel = remember { ContractViewModel(LoanRepositoryImpl(null, null, null)) }
    EasyMoneyTheme(darkTheme = false) {
        ContractScreen(
            onSignSuccess = {},
            onCancel = {},
            viewModel = viewModel,
            loanId = ""
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Contract Screen Dark")
@Composable
private fun ContractScreenDarkPreview() {
    val viewModel = remember { ContractViewModel(LoanRepositoryImpl(null, null,null)) }
    EasyMoneyTheme(darkTheme = true) {
        ContractScreen(
            onSignSuccess = {},
            onCancel = {},
            viewModel = viewModel,
            loanId = ""
        )
    }
}
