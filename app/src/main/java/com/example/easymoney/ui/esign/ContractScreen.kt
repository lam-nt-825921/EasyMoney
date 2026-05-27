package com.example.easymoney.ui.esign

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
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
    onTermsClick: () -> Unit = {},
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
            onConfirm = { otp -> viewModel.verifyOtp(otp, loanId, onSignSuccess) },
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
                onCancelClick = onCancel,
                onTermsClick = onTermsClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            val errorMessage = uiState.errorMessage
            if (uiState.isLoading) {
                ContractLoadingContent()
            } else if (errorMessage != null) {
                ContractErrorContent(
                    message = errorMessage,
                    onRetry = { viewModel.loadContract(loanId) }
                )
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
private fun ContractErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onRetry) {
            Text(text = stringResource(R.string.action_retry))
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
    onCancelClick: () -> Unit,
    onTermsClick: () -> Unit
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
                    append(stringResource(R.string.contract_agree_prefix))
                    pushStringAnnotation(tag = "terms", annotation = "terms")
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)) {
                        append(stringResource(R.string.settings_item_terms))
                    }
                    pop()
                }
                
                ClickableText(
                    text = annotatedString,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset)
                            .firstOrNull()?.let {
                                onTermsClick()
                            }
                    }
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
                        text = stringResource(R.string.loan_management_sign),
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
                    text = stringResource(R.string.loan_management_cancel),
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
