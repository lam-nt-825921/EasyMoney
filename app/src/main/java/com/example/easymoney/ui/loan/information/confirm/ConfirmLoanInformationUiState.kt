package com.example.easymoney.ui.loan.information.confirm

import com.example.easymoney.ui.loan.information.form.LoanInformationFormUiState

data class ConfirmLoanInformationUiState(
    val formData: LoanInformationFormUiState = LoanInformationFormUiState(),
    val isSubmitting: Boolean = false,
    val error: String? = null
)
