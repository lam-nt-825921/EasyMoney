package com.example.easymoney.ui.loan.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.ContractStatus
import com.example.easymoney.domain.model.RepayType
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanManagementViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoanManagementUiState(isLoading = true))
    val uiState: StateFlow<LoanManagementUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val contractsResult = loanRepository.getApprovedContracts()
            val debtsResult = loanRepository.getDebts()
            val cardsResult = paymentRepository.getPaymentCards()

            val error = listOf(contractsResult, debtsResult, cardsResult)
                .filterIsInstance<Resource.Error<*>>()
                .firstOrNull()
                ?.message

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    contracts = if (contractsResult is Resource.Success) {
                        contractsResult.data.filter { it.status == ContractStatus.APPROVED }
                    } else state.contracts,
                    debts = if (debtsResult is Resource.Success) debtsResult.data else state.debts,
                    cards = if (cardsResult is Resource.Success) cardsResult.data else state.cards,
                    errorMessage = error
                )
            }
        }
    }

    fun cancelContract(contractId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, actionMessage = null) }
            when (val result = loanRepository.cancelContract(contractId)) {
                is Resource.Success -> load()
                is Resource.Error -> _uiState.update { it.copy(isSubmitting = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun repayDebt(debtId: Long, repayType: RepayType, cardId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, actionMessage = null, shouldNavigateToAddCard = false) }
            when (val result = loanRepository.repayDebt(debtId, repayType, cardId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, actionMessage = "Thanh toán khoản nợ thành công.") }
                    load()
                }
                is Resource.Error -> _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = result.message.removeNavigationMarker(),
                        shouldNavigateToAddCard = result.message.shouldNavigateToAddCard()
                    )
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, actionMessage = null, shouldNavigateToAddCard = false) }
    }

    fun consumeAddCardNavigation() {
        _uiState.update { it.copy(shouldNavigateToAddCard = false) }
    }
}

private fun String.shouldNavigateToAddCard(): Boolean =
    contains("NAVIGATE_ADD_CARD", ignoreCase = true) ||
        contains("chưa thêm thẻ", ignoreCase = true)

private fun String.removeNavigationMarker(): String =
    replace(" | NAVIGATE_ADD_CARD", "").replace("NAVIGATE_ADD_CARD", "").trim()
