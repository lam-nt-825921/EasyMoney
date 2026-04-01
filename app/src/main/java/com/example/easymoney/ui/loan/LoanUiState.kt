package com.example.easymoney.ui.loan

import com.example.easymoney.domain.model.LoanPackageModel

data class LoanUiState(
    val selectedPackage: LoanPackageModel? = null,
    val currentStep: Int = 1,
    val loanAmount: Long = 0,
    val selectedTenorMonth: Int = 0,
    val isInsuranceSelected: Boolean = true,

    // Calculation results
    val actualReceivedAmount: Long = 0,
    val insuranceFee: Long = 0,
    val interestAmount: Long = 0,
    val monthlyPayment: Long = 0,
    val totalPayment: Long = 0,

    // Data loading state
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val packageLoadState: LoanPackageLoadState = LoanPackageLoadState.Idle
)

sealed class LoanPackageLoadState {
    data object Idle : LoanPackageLoadState()
    data object Loading : LoanPackageLoadState()
    data class Success(val packageId: String) : LoanPackageLoadState()
    data class Error(val message: String) : LoanPackageLoadState()
}
