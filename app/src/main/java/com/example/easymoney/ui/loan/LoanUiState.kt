package com.example.easymoney.ui.loan

import com.example.easymoney.data.model.LoanPackage

data class LoanUiState(
    val selectedPackage: LoanPackage? = null,
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
    
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class LoanApplicationStatus {
    object Idle : LoanApplicationStatus()
    object Loading : LoanApplicationStatus()
    data class Success(val applicationId: String) : LoanApplicationStatus()
    data class Error(val message: String) : LoanApplicationStatus()
}
