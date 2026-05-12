package com.example.easymoney.ui.loan.management

import com.example.easymoney.domain.model.LoanContractModel

sealed interface LoanManagementUiState {
    data object Loading : LoanManagementUiState
    data class Success(
        val contracts: List<LoanContractModel>,
        val pendingCancelId: String? = null
    ) : LoanManagementUiState
    data object Empty : LoanManagementUiState
    data class Error(val message: String) : LoanManagementUiState
}
