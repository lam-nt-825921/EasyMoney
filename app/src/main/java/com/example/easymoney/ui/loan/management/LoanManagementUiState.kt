package com.example.easymoney.ui.loan.management

import com.example.easymoney.domain.model.LoanContractModel
import com.example.easymoney.domain.model.LoanDebtModel
import com.example.easymoney.domain.model.PaymentCard

data class LoanManagementUiState(
    val isLoading: Boolean = false,
    val contracts: List<LoanContractModel> = emptyList(),
    val debts: List<LoanDebtModel> = emptyList(),
    val cards: List<PaymentCard> = emptyList(),
    val errorMessage: String? = null,
    val actionMessage: String? = null,
    val shouldNavigateToAddCard: Boolean = false,
    val isSubmitting: Boolean = false
)
