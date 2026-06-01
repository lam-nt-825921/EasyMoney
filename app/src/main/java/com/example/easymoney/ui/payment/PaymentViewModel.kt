package com.example.easymoney.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.R
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.WalletInfo
import com.example.easymoney.domain.repository.PaymentRepository
import com.example.easymoney.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// Workflow #63 — error/action messages now use UiText (no Android Context in ViewModel).
data class PaymentUiState(
    val walletInfo: WalletInfo? = null,
    val cards: List<PaymentCard> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val actionMessage: UiText? = null,
    val isSubmitting: Boolean = false
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val walletRes = paymentRepository.getWalletInfo()
            val cardsRes = paymentRepository.getPaymentCards()

            val errorRaw = (walletRes as? Resource.Error)?.message
                ?: (cardsRes as? Resource.Error)?.message

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    walletInfo = if (walletRes is Resource.Success) walletRes.data else state.walletInfo,
                    cards = if (cardsRes is Resource.Success) cardsRes.data else state.cards,
                    errorMessage = errorRaw?.let { UiText.DynamicString(it) }
                )
            }
        }
    }

    fun toggleAutoDeduction(enabled: Boolean) {
        viewModelScope.launch {
            paymentRepository.toggleAutoDeduction(enabled)
            loadData()
        }
    }

    fun topUp(amount: Long, cardId: String) {
        viewModelScope.launch {
            paymentRepository.topUp(amount, cardId)
            loadData()
        }
    }

    fun addCard(cardNumber: String, bankName: String, cardType: String) {
        val sanitizedNumber = cardNumber.filter { it.isDigit() }
        when {
            sanitizedNumber.length < 8 -> _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.payment_card_invalid_number))
            }
            bankName.isBlank() -> _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.payment_card_invalid_bank))
            }
            else -> viewModelScope.launch {
                _uiState.update { it.copy(isSubmitting = true, errorMessage = null, actionMessage = null) }
                val card = PaymentCard(
                    id = "card_${System.currentTimeMillis()}",
                    cardNumber = sanitizedNumber,
                    cardType = cardType.ifBlank { "NAPAS" },
                    bankName = bankName.trim()
                )
                when (val result = paymentRepository.addPaymentCard(card)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isSubmitting = false,
                                actionMessage = UiText.StringResource(R.string.payment_card_added)
                            )
                        }
                        loadData()
                    }
                    is Resource.Error -> _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = UiText.DynamicString(result.message))
                    }
                    Resource.Loading -> Unit
                }
            }
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, actionMessage = null) }
            when (val result = paymentRepository.deletePaymentCard(cardId)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            actionMessage = UiText.StringResource(R.string.payment_card_deleted)
                        )
                    }
                    loadData()
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = UiText.DynamicString(result.message))
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun consumeMessages() {
        _uiState.update { it.copy(errorMessage = null, actionMessage = null) }
    }
}
