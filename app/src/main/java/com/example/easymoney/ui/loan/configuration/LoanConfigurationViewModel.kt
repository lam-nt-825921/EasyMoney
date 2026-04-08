package com.example.easymoney.ui.loan.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanPackageModel
import com.example.easymoney.domain.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanConfigurationViewModel @Inject constructor(
    private val loanRepository: LoanRepository
) : ViewModel() {

    private companion object {
        const val INSURANCE_RATE = 0.01
    }

    private val _uiState = MutableStateFlow(LoanConfigurationUiState())
    val uiState: StateFlow<LoanConfigurationUiState> = _uiState.asStateFlow()
    private var hasRequestedPackage = false

    init {
        loadLoanPackage()
    }

    fun loadLoanPackage(force: Boolean = false) {
        if (hasRequestedPackage && !force) return
        hasRequestedPackage = true

        _uiState.update {
            val nextLoadState = if (it.selectedPackage == null) {
                LoanPackageLoadState.InitialLoading
            } else {
                LoanPackageLoadState.Loading
            }

            it.copy(
                isLoading = true,
                errorMessage = null,
                packageLoadState = nextLoadState
            )
        }

        viewModelScope.launch {
            when (val result = loanRepository.getMyPackage()) {
                is Resource.Success -> applyLoanPackage(result.data)
                is Resource.Error -> updateLoadError(result.message)
                is Resource.Loading -> Unit
            }
        }
    }


    fun onAmountChanged(amount: Long) {
        val pkg = _uiState.value.selectedPackage ?: return
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

    private fun applyLoanPackage(loanPackage: LoanPackageModel) {
        _uiState.update {
            it.copy(
                selectedPackage = loanPackage,
                loanAmount = loanPackage.minAmount,
                selectedTenorMonth = loanPackage.getTenorList().firstOrNull() ?: 6,
                isLoading = false,
                errorMessage = null,
                packageLoadState = LoanPackageLoadState.Success(loanPackage.id)
            )
        }
        calculateLoan()
    }

    private fun updateLoadError(message: String) {
        _uiState.update {
            it.copy(
                selectedPackage = null,
                isLoading = false,
                errorMessage = message,
                packageLoadState = LoanPackageLoadState.Error(message)
            )
        }
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
}
