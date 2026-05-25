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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.ui.home.EligibilityUiState
import com.example.easymoney.R
import java.text.NumberFormat
import java.util.Locale

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
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: stringResource(R.string.loan_detail_load_error),
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.error
            )
        } else {
            uiState.selectedPackage?.let { loanPackage ->
                val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())
                val minAmount = numberFormat.format(loanPackage.minAmount)
                val maxAmount = numberFormat.format(loanPackage.maxAmount)
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
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = loanPackage.packageName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.loan_detail_eligibility_note, loanPackage.eligibleCreditScore.toString()),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Details Card
                    DetailSection(title = stringResource(R.string.loan_detail_section_info)) {
                        DetailItem(
                            label = stringResource(R.string.loan_detail_label_amount_limit),
                            value = stringResource(R.string.loan_detail_amount_range, minAmount, maxAmount)
                        )
                        DetailItem(
                            label = stringResource(R.string.loan_detail_label_tenor),
                            value = stringResource(R.string.loan_detail_tenor_months, loanPackage.tenorRange)
                        )
                        DetailItem(
                            label = stringResource(R.string.loan_detail_label_interest),
                            value = stringResource(R.string.loan_detail_interest_year, loanPackage.interest)
                        )
                        DetailItem(
                            label = stringResource(R.string.loan_detail_label_overdue_cost),
                            value = stringResource(R.string.loan_detail_overdue_day, loanPackage.overdueCost)
                        )
                    }

                    // Conditions Card
                    DetailSection(title = stringResource(R.string.loan_detail_section_conditions)) {
                        ConditionItem(text = stringResource(R.string.loan_detail_condition_age))
                        ConditionItem(text = stringResource(R.string.loan_detail_condition_income))
                        ConditionItem(text = stringResource(R.string.loan_detail_condition_credit))
                    }

                    // Illustration Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.loan_detail_example_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
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
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Button(
                        onClick = { viewModel.checkEligibility(packageId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(id = R.string.loan_detail_register_now), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (uiState.eligibilityState is EligibilityUiState.Checking) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
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
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
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
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

