package com.example.easymoney.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.R
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.repository.PaymentRepository
import com.example.easymoney.ui.common.error.BackendErrorCode
import com.example.easymoney.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WithdrawViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        WithdrawUiState(is2FAEnabled = appPreferences.isBiometric2FAEnabled)
    )
    val uiState: StateFlow<WithdrawUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val cardsRes = paymentRepository.getPaymentCards()
            val walletRes = paymentRepository.getWalletInfo()
            val cards = if (cardsRes is Resource.Success) cardsRes.data else emptyList()
            val balance = if (walletRes is Resource.Success) walletRes.data.availableBalance else 0L
            _uiState.update {
                it.copy(cards = cards, balance = balance, selectedCardId = cards.firstOrNull()?.id)
            }
        }
    }

    fun onAmountChange(value: String) {
        _uiState.update {
            it.copy(
                amountText = value.filter { c -> c.isDigit() }.take(12),
                errorMessage = null,
                shouldNavigateToAddCard = false
            )
        }
    }

    fun onSelectCard(id: String) {
        _uiState.update { it.copy(selectedCardId = id) }
    }

    fun onSubmit() {
        val state = _uiState.value
        val amount = state.amountValue
        when {
            amount == null || amount <= 0 -> _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.error_invalid_amount))
            }
            amount > state.balance -> _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.error_insufficient_balance))
            }
            state.cards.isEmpty() -> _uiState.update {
                it.copy(
                    errorMessage = UiText.StringResource(R.string.error_card_required),
                    shouldNavigateToAddCard = true
                )
            }
            state.selectedCardId == null -> _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.withdraw_error_no_destination))
            }
            else -> submit(amount, state.selectedCardId)
        }
    }

    private fun submit(amount: Long, cardId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            android.util.Log.d("Transaction", "withdraw amount=$amount card=$cardId")
            when (val res = paymentRepository.withdraw(amount, cardId, biometricToken = null)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            successMessage = UiText.StringResource(R.string.withdraw_success),
                            amountText = "",
                            balance = it.balance - amount
                        )
                    }
                }
                is Resource.Error -> {
                    // Workflow #63 — map backend error code → localised resource when possible;
                    // unknown messages fall through as raw DynamicString (logged as such).
                    val backendCode = BackendErrorCode.detect(res.message)
                    val errorText: UiText = backendCode?.let { UiText.StringResource(it.resId) }
                        ?: UiText.DynamicString(res.message.removeNavigationMarker())
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = errorText,
                            shouldNavigateToAddCard = res.message.shouldNavigateToAddCard()
                        )
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun consumeMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null, shouldNavigateToAddCard = false) }
    }

    /** Workflow #64 — biometric gate huỷ/fail trước khi gọi backend. */
    fun onBiometricCancelled(message: String) {
        _uiState.update { it.copy(errorMessage = UiText.DynamicString(message)) }
    }

    fun consumeAddCardNavigation() {
        _uiState.update { it.copy(shouldNavigateToAddCard = false) }
    }
}

// Workflow #62 — phát hiện cả mã backend `CARD_REQUIRED` lẫn marker FE.
private fun String.shouldNavigateToAddCard(): Boolean =
    contains("NAVIGATE_ADD_CARD", ignoreCase = true) ||
        contains("CARD_REQUIRED", ignoreCase = true) ||
        contains("chưa thêm thẻ", ignoreCase = true)

private fun String.removeNavigationMarker(): String =
    replace(" | NAVIGATE_ADD_CARD", "")
        .replace("NAVIGATE_ADD_CARD", "")
        .replace("CARD_REQUIRED", "")
        .trim()
