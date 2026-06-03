package com.example.easymoney.ui.loan.management

import com.example.easymoney.domain.model.LoanContractModel
import com.example.easymoney.domain.model.LoanDebtModel
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.RepaymentEstimate
import com.example.easymoney.utils.UiText

data class LoanManagementUiState(
    val isLoading: Boolean = false,
    val contracts: List<LoanContractModel> = emptyList(),
    val debts: List<LoanDebtModel> = emptyList(),
    val cards: List<PaymentCard> = emptyList(),
    // Workflow #63 — error/action messages now use UiText.
    val errorMessage: UiText? = null,
    val actionMessage: UiText? = null,
    val shouldNavigateToAddCard: Boolean = false,
    // Workflow #90 — isSubmitting drives the debt/repay loading state only.
    val isSubmitting: Boolean = false,
    // Workflow #90 — cancel state scoped to the affected contract so cancelling one contract does
    // not disable the buttons on every other contract card.
    val submittingContractId: String? = null,
    // Workflow #71 — repayment estimate shown in the repay dialog before confirming.
    val estimate: RepaymentEstimate? = null,
    val isEstimateLoading: Boolean = false,
    val estimateError: UiText? = null
)
