package com.example.easymoney.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WithdrawViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WithdrawUiState())
    val uiState: StateFlow<WithdrawUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    private fun load() {
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
        _uiState.update { it.copy(amountText = value.filter { c -> c.isDigit() }.take(12), errorMessage = null) }
    }

    fun onSelectCard(id: String) {
        _uiState.update { it.copy(selectedCardId = id) }
    }

    fun onSubmit() {
        val state = _uiState.value
        val amount = state.amountValue
        when {
            amount == null || amount <= 0 -> _uiState.update { it.copy(errorMessage = "Số tiền không hợp lệ") }
            amount > state.balance -> _uiState.update { it.copy(errorMessage = "Số dư không đủ") }
            state.selectedCardId == null -> _uiState.update { it.copy(errorMessage = "Chưa chọn đích đến") }
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
                            successMessage = "Rút tiền thành công",
                            amountText = "",
                            balance = it.balance - amount
                        )
                    }
                }
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = res.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun consumeMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
