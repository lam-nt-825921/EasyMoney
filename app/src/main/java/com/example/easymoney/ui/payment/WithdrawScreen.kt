package com.example.easymoney.ui.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WithdrawScreen(
    viewModel: WithdrawViewModel = hiltViewModel(),
    onWithdrawSuccess: () -> Unit = {},
    onNavigateToAddCard: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var amountFieldValue by remember {
        mutableStateOf(TextFieldValue(uiState.amountText, selection = TextRange(uiState.amountText.length)))
    }

    LaunchedEffect(uiState.amountText) {
        if (uiState.amountText != amountFieldValue.text) {
            amountFieldValue = TextFieldValue(
                uiState.amountText,
                selection = TextRange(uiState.amountText.length)
            )
        }
    }

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onWithdrawSuccess()
            viewModel.consumeMessages()
        }
    }

    LaunchedEffect(uiState.shouldNavigateToAddCard) {
        if (uiState.shouldNavigateToAddCard) {
            viewModel.consumeAddCardNavigation()
            onNavigateToAddCard()
        }
    }

    val balanceFormatted = NumberFormat.getInstance(Locale("vi", "VN")).format(uiState.balance)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            stringResource(R.string.withdraw_balance, balanceFormatted),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = amountFieldValue,
            onValueChange = { candidate ->
                val sanitized = candidate.text.filter(Char::isDigit).take(12)
                amountFieldValue = TextFieldValue(sanitized, selection = TextRange(sanitized.length))
                viewModel.onAmountChange(sanitized)
            },
            placeholder = { Text(stringResource(R.string.withdraw_amount_placeholder)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.withdraw_select_dest), style = MaterialTheme.typography.titleMedium)
        if (uiState.cards.isEmpty()) {
            Text(
                stringResource(R.string.card_empty_state),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
            OutlinedButton(
                onClick = onNavigateToAddCard,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.card_action_add))
            }
        } else {
            uiState.cards.forEach { card ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = card.id == uiState.selectedCardId,
                        onClick = { viewModel.onSelectCard(card.id) }
                    )
                    Text("${card.bankName} ${card.cardNumber}")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        uiState.errorMessage?.let { msg ->
            Text(msg.asString(), color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.onSubmit() },
            enabled = !uiState.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(stringResource(R.string.withdraw_button))
            }
        }
    }
}
