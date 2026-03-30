package com.example.easymoney.ui.loan

import androidx.lifecycle.ViewModel
import com.example.easymoney.data.UserPreferencesRepository
import com.example.easymoney.data.model.LoanPackage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoanViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private companion object {
        const val INSURANCE_RATE = 0.01
    }

    private val _uiState = MutableStateFlow(LoanUiState())
    val uiState: StateFlow<LoanUiState> = _uiState.asStateFlow()

    /**
     * Call this when the screen is initialized with a package
     */
    fun setLoanPackage(loanPackage: LoanPackage) {
        _uiState.update {
            it.copy(
                selectedPackage = loanPackage,
                loanAmount = loanPackage.minAmount, // Default to min
                selectedTenorMonth = loanPackage.getTenorList().firstOrNull() ?: 6
            )
        }
        calculateLoan()
    }

    fun onAmountChanged(amount: Long) {
        val pkg = _uiState.value.selectedPackage ?: return
        // Ensure amount stays within package limits
        val coercedAmount = amount.coerceIn(pkg.minAmount, pkg.maxAmount)
        _uiState.update { it.copy(loanAmount = coercedAmount) }
        calculateLoan()
    }

    fun onTenorSelected(months: Int) {
        val pkg = _uiState.value.selectedPackage ?: return
        val tenors = pkg.getTenorList()
        if (months !in tenors) return

        _uiState.update { it.copy(selectedTenorMonth = months) }
        calculateLoan()
    }

    fun onInsuranceToggled(selected: Boolean) {
        _uiState.update { it.copy(isInsuranceSelected = selected) }
        calculateLoan()
    }

    private fun calculateLoan() {
        val state = _uiState.value
        val pkg = state.selectedPackage ?: return
        
        val amount = state.loanAmount
        val tenor = state.selectedTenorMonth.coerceAtLeast(1)
        val interestRatePerYear = pkg.interest / 100.0
        val insuranceFee = if (state.isInsuranceSelected) (amount * INSURANCE_RATE).toLong() else 0L

        val interestAmount = (amount * interestRatePerYear * tenor / 12.0).toLong()
        val totalPayment = amount + insuranceFee + interestAmount
        val monthlyPayment = (totalPayment / tenor.toDouble()).toLong()
        val actualReceived = (amount - insuranceFee).coerceAtLeast(0L)

        _uiState.update {
            it.copy(
                actualReceivedAmount = actualReceived,
                insuranceFee = insuranceFee,
                interestAmount = interestAmount,
                monthlyPayment = monthlyPayment,
                totalPayment = totalPayment
            )
        }
    }

    fun onNextStep() {
        _uiState.update { it.copy(currentStep = it.currentStep + 1) }
    }
}
