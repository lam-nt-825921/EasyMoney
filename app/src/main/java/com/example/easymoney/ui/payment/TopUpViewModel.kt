package com.example.easymoney.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.R
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.repository.PaymentRepository
import com.example.easymoney.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopUpViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    companion object {
        const val MIN_AMOUNT = 10_000L
        const val MAX_AMOUNT = 50_000_000L
    }

    private val _uiState = MutableStateFlow(TopUpUiState())
    val uiState: StateFlow<TopUpUiState> = _uiState.asStateFlow()

    init {
        loadCards()
    }

    private fun loadCards() {
        viewModelScope.launch {
            when (val res = paymentRepository.getPaymentCards()) {
                is Resource.Success -> _uiState.update {
                    it.copy(cards = res.data, selectedCardId = res.data.firstOrNull()?.id)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(errorMessage = UiText.DynamicString(res.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onAmountChange(value: String) {
        val sanitized = value.filter { it.isDigit() }.take(12)
        _uiState.update { it.copy(amountText = sanitized, errorMessage = null) }
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
            amount < MIN_AMOUNT -> _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.topup_error_below_min, "10.000"))
            }
            amount > MAX_AMOUNT -> _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.topup_error_above_max))
            }
            state.selectedCardId == null -> _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.topup_error_no_card_selected))
            }
            else -> submit(amount, state.selectedCardId)
        }
    }

    private fun submit(amount: Long, cardId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            android.util.Log.d("Transaction", "topup amount=$amount card=$cardId")
            when (val res = paymentRepository.topUp(amount, cardId)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        successMessage = UiText.StringResource(R.string.topup_success),
                        amountText = ""
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = UiText.DynamicString(res.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun consumeMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
