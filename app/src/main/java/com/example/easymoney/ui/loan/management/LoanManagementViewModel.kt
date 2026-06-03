package com.example.easymoney.ui.loan.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.R
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.ContractStatus
import com.example.easymoney.domain.model.RepayType
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.domain.repository.PaymentRepository
import com.example.easymoney.domain.repository.RewardRepository
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
class LoanManagementViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val paymentRepository: PaymentRepository,
    private val rewardRepository: RewardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoanManagementUiState(isLoading = true))
    val uiState: StateFlow<LoanManagementUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /**
     * Workflow #71 — fetch the estimate for the currently selected debt + payment source so the
     * user can see amount due / fees / reward preview before confirming. Never guesses in REMOTE:
     * a failure surfaces [LoanManagementUiState.estimateError] for an explicit retry.
     */
    fun loadEstimate(debtId: Long, repayType: RepayType, cardId: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEstimateLoading = true, estimate = null, estimateError = null) }
            when (val result = loanRepository.getRepaymentEstimate(debtId, repayType, cardId)) {
                is Resource.Success -> _uiState.update {
                    it.copy(isEstimateLoading = false, estimate = result.data, estimateError = null)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isEstimateLoading = false, estimate = null, estimateError = UiText.DynamicString(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearEstimate() {
        _uiState.update { it.copy(estimate = null, isEstimateLoading = false, estimateError = null) }
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
                    errorMessage = error?.let { UiText.DynamicString(it) }
                )
            }
        }
    }

    fun cancelContract(contractId: String) {
        viewModelScope.launch {
            // Workflow #90 — scope submitting to this contract only; reset on every terminal path
            // (success and error) so other contract cards stay interactive without leaving the screen.
            _uiState.update {
                it.copy(submittingContractId = contractId, errorMessage = null, actionMessage = null)
            }
            when (val result = loanRepository.cancelContract(contractId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(submittingContractId = null) }
                    load()
                }
                is Resource.Error -> _uiState.update {
                    it.copy(submittingContractId = null, errorMessage = UiText.DynamicString(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun repayDebt(debtId: Long, repayType: RepayType, cardId: String?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, errorMessage = null, actionMessage = null, shouldNavigateToAddCard = false)
            }
            when (val result = loanRepository.repayDebt(debtId, repayType, cardId)) {
                is Resource.Success -> {
                    // Workflow #71 — refresh reward points from /api/v1/rewards/user after a
                    // successful repay so the user sees the points they just earned.
                    val pointsMessage = when (val rewards = rewardRepository.getRewardsCatalog()) {
                        is Resource.Success -> UiText.StringResource(
                            R.string.loan_mgmt_repay_success_points,
                            rewards.data.totalPoints
                        )
                        else -> UiText.StringResource(R.string.loan_mgmt_repay_success)
                    }
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            estimate = null,
                            estimateError = null,
                            actionMessage = pointsMessage
                        )
                    }
                    load()
                }
                is Resource.Error -> {
                    // Workflow #63 — map backend code → localised resource; unknowns fall through.
                    val backendCode = BackendErrorCode.detect(result.message)
                    val errorText: UiText = backendCode?.let { UiText.StringResource(it.resId) }
                        ?: UiText.DynamicString(result.message.removeNavigationMarker())
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = errorText,
                            shouldNavigateToAddCard = result.message.shouldNavigateToAddCard()
                        )
                    }
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
