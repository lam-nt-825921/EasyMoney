package com.example.easymoney.ui.loan.management

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
import com.example.easymoney.domain.model.LoanContractModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoanManagementScreen(
    viewModel: LoanManagementViewModel = hiltViewModel(),
    onSignContract: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var pendingCancelId by remember { mutableStateOf<String?>(null) }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (val state = uiState) {
            is LoanManagementUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is LoanManagementUiState.Empty -> {
                Text(
                    text = stringResource(R.string.loan_management_empty),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            is LoanManagementUiState.Error -> {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { viewModel.load() }) {
                        Text(stringResource(R.string.action_retry))
                    }
                }
            }
            is LoanManagementUiState.Success -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.contracts, key = { it.id }) { contract ->
                        ContractCard(
                            contract = contract,
                            onCancel = { pendingCancelId = contract.id },
                            onSign = { onSignContract(contract.id) }
                        )
                    }
                }
            }
        }

        pendingCancelId?.let { id ->
            AlertDialog(
                onDismissRequest = { pendingCancelId = null },
                title = { Text(stringResource(R.string.loan_management_cancel_title)) },
                text = { Text(stringResource(R.string.loan_management_cancel_confirm)) },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.cancelContract(id)
                        pendingCancelId = null
                    }) {
                        Text(stringResource(R.string.action_confirm))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pendingCancelId = null }) {
                        Text(stringResource(R.string.action_dismiss))
                    }
                }
            )
        }
    }
}

@Composable
private fun ContractCard(
    contract: LoanContractModel,
    onCancel: () -> Unit,
    onSign: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = contract.contractNumber,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            val amountFormatted = NumberFormat.getInstance(Locale("vi", "VN")).format(contract.amount)
            Text(
                text = stringResource(R.string.loan_management_amount, amountFormatted),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.loan_management_term, contract.termMonths),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.loan_management_rate, contract.interestRate),
                style = MaterialTheme.typography.bodyMedium
            )
            val approvedAt = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).format(Date(contract.approvedAt))
            Text(
                text = stringResource(R.string.loan_management_approved_at, approvedAt),
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.loan_management_cancel))
                }
                Button(
                    onClick = onSign,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.loan_management_sign))
                }
            }
        }
    }
}
