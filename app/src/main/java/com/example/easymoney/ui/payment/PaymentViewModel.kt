package com.example.easymoney.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.R
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.AddCardOutcome
import com.example.easymoney.domain.model.AddCardRequest
import com.example.easymoney.domain.model.Bank
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.WalletInfo
import com.example.easymoney.domain.repository.PaymentRepository
import com.example.easymoney.utils.UiText
import java.util.Calendar
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
    val isSubmitting: Boolean = false,
    // Workflow #75 — add-card form: bank options, inline field errors, form-level error, success signal.
    val banks: List<Bank> = emptyList(),
    val cardFieldErrors: Map<String, UiText> = emptyMap(),
    val cardFormError: UiText? = null,
    val cardAddedSuccess: Boolean = false
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
            val banksRes = paymentRepository.getBanks()

            val errorRaw = (walletRes as? Resource.Error)?.message
                ?: (cardsRes as? Resource.Error)?.message

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    walletInfo = if (walletRes is Resource.Success) walletRes.data else state.walletInfo,
                    cards = if (cardsRes is Resource.Success) cardsRes.data else state.cards,
                    banks = if (banksRes is Resource.Success) banksRes.data else state.banks,
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

    /**
     * Workflow #75 — validate client-side first, then verify+add via the backend. Field errors
     * (client or backend) are surfaced inline; the form state lives in the dialog and is never
     * cleared on failure. Only [PaymentUiState.cardAddedSuccess] signals the dialog to close.
     */
    fun submitAddCard(request: AddCardRequest) {
        val clientErrors = validateCard(request)
        if (clientErrors.isNotEmpty()) {
            _uiState.update { it.copy(cardFieldErrors = clientErrors, cardFormError = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, cardFieldErrors = emptyMap(), cardFormError = null, errorMessage = null, actionMessage = null)
            }
            when (val outcome = paymentRepository.addCard(request)) {
                AddCardOutcome.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            cardAddedSuccess = true,
                            actionMessage = UiText.StringResource(R.string.payment_card_added)
                        )
                    }
                    loadData()
                }
                is AddCardOutcome.FieldErrors -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        cardFieldErrors = outcome.fieldErrors.mapValues { (_, msg) -> UiText.DynamicString(msg) },
                        cardFormError = outcome.message?.let { m -> UiText.DynamicString(m) }
                    )
                }
                is AddCardOutcome.Failure -> _uiState.update {
                    it.copy(isSubmitting = false, cardFormError = UiText.DynamicString(outcome.message))
                }
            }
        }
    }

    private fun validateCard(request: AddCardRequest): Map<String, UiText> {
        val errors = mutableMapOf<String, UiText>()
        if (request.bankId.isBlank()) {
            errors["bank_id"] = UiText.StringResource(R.string.add_card_error_bank_required)
        }
        if (request.cardType.isBlank()) {
            errors["card_type"] = UiText.StringResource(R.string.add_card_error_type_required)
        }
        val digits = request.cardNumber.filter { it.isDigit() }
        if (digits.length !in 16..19) {
            errors["card_number"] = UiText.StringResource(R.string.add_card_error_number)
        }
        if (request.cardHolderName.isBlank()) {
            errors["card_holder_name"] = UiText.StringResource(R.string.add_card_error_holder)
        }
        if (!isExpiryValid(request.expiryMonth, request.expiryYear)) {
            errors["expiry"] = UiText.StringResource(R.string.add_card_error_expiry)
        }
        return errors
    }

    private fun isExpiryValid(monthStr: String, yearStr: String): Boolean {
        val month = monthStr.toIntOrNull() ?: return false
        val year = yearStr.toIntOrNull() ?: return false
        if (month !in 1..12) return false
        val fullYear = if (year < 100) 2000 + year else year
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        return when {
            fullYear < currentYear -> false
            fullYear == currentYear && month < currentMonth -> false
            else -> true
        }
    }

    fun clearCardForm() {
        _uiState.update { it.copy(cardFieldErrors = emptyMap(), cardFormError = null, cardAddedSuccess = false) }
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
