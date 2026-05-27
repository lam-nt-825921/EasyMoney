package com.example.easymoney.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.ui.theme.TealPrimary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentCardsScreen(
    onBack: () -> Unit,
    onAddCard: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<PaymentCard?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(uiState.cards, key = { it.id }) { card ->
                CreditCardItem(
                    card = card,
                    enabled = !uiState.isSubmitting,
                    onDelete = { pendingDelete = card }
                )
            }
            
            item {
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(TealPrimary, TealPrimary)))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.money_mgmt_add_card), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }

    if (showAddDialog) {
        AddCardDialog(
            isSubmitting = uiState.isSubmitting,
            onDismiss = { showAddDialog = false },
            onSubmit = { number, bank, type ->
                viewModel.addCard(number, bank, type)
                showAddDialog = false
            }
        )
    }

    pendingDelete?.let { card ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Xóa thẻ") },
            text = { Text("Bạn có chắc muốn xóa thẻ ${card.bankName} ${maskCardNumber(card.cardNumber)}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCard(card.id)
                    pendingDelete = null
                }) {
                    Text(stringResource(R.string.action_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.action_dismiss))
                }
            }
        )
    }

    uiState.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::consumeMessages,
            title = { Text(stringResource(R.string.dialog_error_title)) },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::consumeMessages) {
                    Text(stringResource(R.string.dialog_button_close))
                }
            }
        )
    }

    uiState.actionMessage?.let { message ->
        AlertDialog(
            onDismissRequest = viewModel::consumeMessages,
            title = { Text("Thông báo") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = viewModel::consumeMessages) {
                    Text(stringResource(R.string.action_ok))
                }
            }
        )
    }
}

@Composable
private fun CreditCardItem(
    card: PaymentCard,
    enabled: Boolean,
    onDelete: () -> Unit
) {
    val gradient = Brush.linearGradient(
        colors = listOf(Color(0xFF1A237E), Color(0xFF0D47A1))
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        shadowElevation = 4.dp
    ) {
        Box(modifier = Modifier.background(gradient).padding(24.dp)) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = card.bankName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        IconButton(onClick = onDelete, enabled = enabled) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa thẻ", tint = Color.White)
                        }
                    }
                }
                
                Text(
                    text = maskCardNumber(card.cardNumber),
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    letterSpacing = 2.sp
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(text = stringResource(R.string.money_mgmt_card_holder), color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                        Text(text = "NGUYEN VAN A", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "BALANCE", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                        Text(text = "${formatMoney(card.balance)}đ", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddCardDialog(
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var cardType by remember { mutableStateOf("NAPAS") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.money_mgmt_add_card)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it.filter(Char::isDigit).take(19) },
                    label = { Text("Số thẻ") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it.take(40) },
                    label = { Text("Ngân hàng") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = cardType,
                    onValueChange = { cardType = it.take(16).uppercase() },
                    label = { Text("Loại thẻ") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = { onSubmit(cardNumber, bankName, cardType) }
            ) {
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

private fun maskCardNumber(value: String): String {
    val digits = value.filter(Char::isDigit)
    if (digits.length <= 4) return value
    return "**** **** **** ${digits.takeLast(4)}"
}

private fun formatMoney(value: Long): String =
    NumberFormat.getInstance(Locale("vi", "VN")).format(value)

