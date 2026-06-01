package com.example.easymoney.ui.loan.management

import com.example.easymoney.domain.model.LoanContractModel
import com.example.easymoney.domain.model.LoanDebtModel
import com.example.easymoney.domain.model.PaymentCard
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
    val isSubmitting: Boolean = false,
    // Workflow #64 — biometric 2FA local toggle.
    val is2FAEnabled: Boolean = false
)
