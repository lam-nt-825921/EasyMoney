package com.example.easymoney.ui.loan.management

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
import com.example.easymoney.domain.model.LoanContractModel
import com.example.easymoney.domain.model.LoanDebtModel
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.RepayType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoanManagementScreen(
    viewModel: LoanManagementViewModel = hiltViewModel(),
    initialDebtId: String? = null,
    onSignContract: (String) -> Unit = {},
    onNavigateToAddCard: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember(initialDebtId) { mutableIntStateOf(if (initialDebtId.isNullOrBlank()) 0 else 1) }
    var pendingCancelId by remember { mutableStateOf<String?>(null) }
    var pendingRepay by remember { mutableStateOf<Pair<Long, RepayType>?>(null) }
    var selectedRepayCardId by remember(pendingRepay) { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(uiState.shouldNavigateToAddCard) {
        if (uiState.shouldNavigateToAddCard) {
            viewModel.consumeAddCardNavigation()
            onNavigateToAddCard()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Hợp đồng chờ ký") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Khoản nợ hiện tại") }
            )
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                selectedTab == 0 -> ContractList(
                    contracts = uiState.contracts,
                    isSubmitting = uiState.isSubmitting,
                    onCancel = { pendingCancelId = it },
                    onSign = onSignContract,
                    modifier = Modifier.fillMaxSize()
                )
                else -> DebtList(
                    debts = uiState.debts,
                    isSubmitting = uiState.isSubmitting,
                    onRepay = { debtId, type -> pendingRepay = debtId to type },
                    modifier = Modifier.fillMaxSize()
                )
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

    pendingRepay?.let { (debtId, type) ->
        RepayDialog(
            repayType = type,
            cards = uiState.cards,
            selectedCardId = selectedRepayCardId,
            onSelectWallet = { selectedRepayCardId = null },
            onSelectCard = { selectedRepayCardId = it },
            onAddCard = {
                pendingRepay = null
                onNavigateToAddCard()
            },
            onDismiss = { pendingRepay = null },
            onConfirm = {
                viewModel.repayDebt(debtId, type, selectedRepayCardId)
                pendingRepay = null
            }
        )
    }

    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearMessages,
            title = { Text(stringResource(R.string.dialog_error_title)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::clearMessages) {
                    Text(stringResource(R.string.dialog_button_close))
                }
            }
        )
    }

    uiState.actionMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::clearMessages,
            title = { Text("Thông báo") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::clearMessages) {
                    Text(stringResource(R.string.action_ok))
                }
            }
        )
    }
}

@Composable
private fun RepayDialog(
    repayType: RepayType,
    cards: List<PaymentCard>,
    selectedCardId: String?,
    onSelectWallet: () -> Unit,
    onSelectCard: (String) -> Unit,
    onAddCard: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (repayType == RepayType.MONTHLY) "Thanh toán kỳ này" else "Tất toán sớm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (repayType == RepayType.MONTHLY) "Chọn nguồn thanh toán cho kỳ nợ hiện tại." else "Chọn nguồn thanh toán để tất toán khoản vay.")
                PaymentOptionRow(
                    selected = selectedCardId == null,
                    title = "Ví EasyMoney",
                    subtitle = "Trừ tiền từ số dư ví",
                    onClick = onSelectWallet
                )
                Text("Thẻ ngân hàng", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                if (cards.isEmpty()) {
                    Text(
                        "Bạn chưa thêm thẻ ngân hàng.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(onClick = onAddCard, modifier = Modifier.fillMaxWidth()) {
                        Text("Thêm thẻ")
                    }
                } else {
                    cards.forEach { card ->
                        PaymentOptionRow(
                            selected = selectedCardId == card.id,
                            title = "${card.bankName} ${card.cardNumber}",
                            subtitle = "Số dư thẻ: ${formatMoney(card.balance)} đ",
                            onClick = { onSelectCard(card.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_dismiss))
            }
        }
    )
}

@Composable
private fun PaymentOptionRow(
    selected: Boolean,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ContractList(
    contracts: List<LoanContractModel>,
    isSubmitting: Boolean,
    onCancel: (String) -> Unit,
    onSign: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (contracts.isEmpty()) {
        Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = stringResource(R.string.loan_management_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(contracts, key = { it.id }) { contract ->
            ContractCard(
                contract = contract,
                enabled = !isSubmitting,
                onCancel = { onCancel(contract.id) },
                onSign = { onSign(contract.id) }
            )
        }
    }
}

@Composable
private fun DebtList(
    debts: List<LoanDebtModel>,
    isSubmitting: Boolean,
    onRepay: (Long, RepayType) -> Unit,
    modifier: Modifier = Modifier
) {
    if (debts.isEmpty()) {
        Box(modifier = modifier.padding(16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "Bạn chưa có khoản nợ đang hoạt động",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(debts, key = { it.id }) { debt ->
            DebtCard(
                debt = debt,
                enabled = !isSubmitting && debt.status == "ACTIVE",
                onMonthlyRepay = { onRepay(debt.id, RepayType.MONTHLY) },
                onFullRepay = { onRepay(debt.id, RepayType.FULL_EARLY) }
            )
        }
    }
}

@Composable
private fun ContractCard(
    contract: LoanContractModel,
    enabled: Boolean,
    onCancel: () -> Unit,
    onSign: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = contract.contractNumber,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            val amountFormatted = formatMoney(contract.amount)
            Text(text = stringResource(R.string.loan_management_amount, amountFormatted), style = MaterialTheme.typography.bodyMedium)
            Text(text = stringResource(R.string.loan_management_term, contract.termMonths), style = MaterialTheme.typography.bodyMedium)
            Text(text = stringResource(R.string.loan_management_rate, contract.interestRate), style = MaterialTheme.typography.bodyMedium)
            val approvedAt = SimpleDateFormat("dd/MM/yyyy", Locale("vi", "VN")).format(Date(contract.approvedAt))
            Text(text = stringResource(R.string.loan_management_approved_at, approvedAt), style = MaterialTheme.typography.bodySmall)

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text(stringResource(R.string.loan_management_cancel))
                }
                Button(
                    onClick = onSign,
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.loan_management_sign))
                }
            }
        }
    }
}

@Composable
private fun DebtCard(
    debt: LoanDebtModel,
    enabled: Boolean,
    onMonthlyRepay: () -> Unit,
    onFullRepay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = debt.applicationId,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            DebtRow("Trạng thái", debt.status)
            DebtRow("Dư nợ gốc", "${formatMoney(debt.remainingPrincipal)} đ")
            DebtRow("Thanh toán tháng", "${formatMoney(debt.monthlyPayment)} đ")
            DebtRow("Lãi suất", "${debt.interestRate}%/tháng")
            DebtRow("Tiến độ", "${debt.monthsPaid}/${debt.totalMonths} tháng")
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onMonthlyRepay,
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Trả kỳ này")
                }
                Button(
                    onClick = onFullRepay,
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Tất toán")
                }
            }
        }
    }
}

@Composable
private fun DebtRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text(value, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

private fun formatMoney(value: Long): String =
    NumberFormat.getInstance(Locale("vi", "VN")).format(value)
