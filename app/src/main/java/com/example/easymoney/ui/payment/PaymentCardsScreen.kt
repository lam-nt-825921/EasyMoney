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
import com.example.easymoney.domain.model.AddCardRequest
import com.example.easymoney.domain.model.Bank
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.ui.theme.TealPrimary
import com.example.easymoney.utils.UiText
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
        // Workflow #75 — close only on success; on validation failure the dialog stays open
        // with the user's input preserved and inline errors shown.
        LaunchedEffect(uiState.cardAddedSuccess) {
            if (uiState.cardAddedSuccess) {
                showAddDialog = false
                viewModel.clearCardForm()
            }
        }
        AddCardDialog(
            banks = uiState.banks,
            isSubmitting = uiState.isSubmitting,
            fieldErrors = uiState.cardFieldErrors,
            formError = uiState.cardFormError,
            onDismiss = {
                showAddDialog = false
                viewModel.clearCardForm()
            },
            onSubmit = { request -> viewModel.submitAddCard(request) }
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
            text = { Text(message.asString()) },
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
            title = { Text(stringResource(R.string.dialog_notice_title)) },
            text = { Text(message.asString()) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCardDialog(
    banks: List<Bank>,
    isSubmitting: Boolean,
    fieldErrors: Map<String, UiText>,
    formError: UiText?,
    onDismiss: () -> Unit,
    onSubmit: (AddCardRequest) -> Unit
) {
    var selectedBank by remember { mutableStateOf<Bank?>(null) }
    var cardType by remember { mutableStateOf("") }
    var cardNumber by remember { mutableStateOf("") }
    var cardHolder by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }

    val cardTypeOptions = selectedBank?.supportedCardTypes?.takeIf { it.isNotEmpty() }
        ?: listOf("DEBIT", "CREDIT")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.money_mgmt_add_card)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                formError?.let { msg ->
                    Text(
                        text = msg.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                DropdownField(
                    label = stringResource(R.string.add_card_bank_label),
                    selectedText = selectedBank?.name ?: "",
                    options = banks.map { it.name },
                    onSelectIndex = { idx ->
                        selectedBank = banks[idx]
                        // Reset card type if the new bank doesn't support the current selection.
                        val supported = banks[idx].supportedCardTypes
                        if (supported.isNotEmpty() && cardType !in supported) cardType = ""
                    },
                    error = fieldErrors["bank_id"]?.asString()
                )

                DropdownField(
                    label = stringResource(R.string.add_card_type_label),
                    selectedText = cardType,
                    options = cardTypeOptions,
                    onSelectIndex = { idx -> cardType = cardTypeOptions[idx] },
                    error = fieldErrors["card_type"]?.asString()
                )

                CardTextField(
                    value = cardNumber,
                    onValueChange = { cardNumber = it.filter(Char::isDigit).take(19) },
                    label = stringResource(R.string.add_card_number_label),
                    error = fieldErrors["card_number"]?.asString()
                )

                CardTextField(
                    value = cardHolder,
                    onValueChange = { cardHolder = it.uppercase().take(40) },
                    label = stringResource(R.string.add_card_holder_label),
                    error = fieldErrors["card_holder_name"]?.asString()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CardTextField(
                        value = expiryMonth,
                        onValueChange = { expiryMonth = it.filter(Char::isDigit).take(2) },
                        label = stringResource(R.string.add_card_expiry_month_label),
                        modifier = Modifier.weight(1f),
                        error = if (fieldErrors.containsKey("expiry")) "" else null
                    )
                    CardTextField(
                        value = expiryYear,
                        onValueChange = { expiryYear = it.filter(Char::isDigit).take(4) },
                        label = stringResource(R.string.add_card_expiry_year_label),
                        modifier = Modifier.weight(1f),
                        error = if (fieldErrors.containsKey("expiry")) "" else null
                    )
                }
                fieldErrors["expiry"]?.let { msg ->
                    Text(
                        text = msg.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                CardTextField(
                    value = cvv,
                    onValueChange = { cvv = it.filter(Char::isDigit).take(4) },
                    label = stringResource(R.string.add_card_cvv_label),
                    error = fieldErrors["cvv"]?.asString()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = {
                    // Workflow #80 — chuẩn hoá expiry về MM/YYYY trước khi gửi backend.
                    val normalizedExpiry =
                        if (expiryMonth.isNotBlank() && expiryYear.isNotBlank()) {
                            "${expiryMonth.padStart(2, '0')}/$expiryYear"
                        } else {
                            ""
                        }
                    onSubmit(
                        AddCardRequest(
                            bankId = selectedBank?.id.orEmpty(),
                            bankName = selectedBank?.name.orEmpty(),
                            cardType = cardType,
                            cardNumber = cardNumber,
                            cardHolderName = cardHolder.trim(),
                            expiry = normalizedExpiry,
                            cvv = cvv
                        )
                    )
                }
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

@Composable
private fun CardTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = error != null,
            modifier = Modifier.fillMaxWidth()
        )
        if (!error.isNullOrBlank()) {
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    selectedText: String,
    options: List<String>,
    onSelectIndex: (Int) -> Unit,
    error: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                isError = error != null,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelectIndex(index)
                            expanded = false
                        }
                    )
                }
            }
        }
        if (!error.isNullOrBlank()) {
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun maskCardNumber(value: String): String {
    val digits = value.filter(Char::isDigit)
    if (digits.length <= 4) return value
    return "**** **** **** ${digits.takeLast(4)}"
}

private fun formatMoney(value: Long): String =
    NumberFormat.getInstance(Locale("vi", "VN")).format(value)

