package com.example.easymoney.ui.loan.configuration

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanQuoteRequest
import com.example.easymoney.domain.model.LoanPackageModel
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanConfigurationViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoanConfigurationUiState())
    val uiState: StateFlow<LoanConfigurationUiState> = _uiState.asStateFlow()
    private var hasRequestedPackage = false
    private var quoteJob: Job? = null
    private val packageId: String? = savedStateHandle[AppDestination.LoanFlow.PACKAGE_ID_ARG]

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
            val result = if (!packageId.isNullOrBlank()) {
                loanRepository.getLoanPackageById(packageId)
            } else {
                loanRepository.getMyPackage()
            }
            when (result) {
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
        refreshVouchers()
        requestQuote()
    }

    fun onTenorSelected(months: Int) {
        val pkg = _uiState.value.selectedPackage ?: return
        val tenors = pkg.getTenorList()
        if (months !in tenors) return

        _uiState.update { it.copy(selectedTenorMonth = months) }
        requestQuote()
    }

    fun onInsuranceToggled(selected: Boolean) {
        _uiState.update { it.copy(isInsuranceSelected = selected) }
        requestQuote()
    }

    fun onVoucherSelected(voucherId: String?) {
        _uiState.update { it.copy(selectedVoucherId = voucherId) }
        requestQuote()
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
        refreshVouchers()
        requestQuote()
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

    private fun refreshVouchers() {
        val state = _uiState.value
        val pkg = state.selectedPackage ?: return
        viewModelScope.launch {
            when (val result = loanRepository.getApplicableVouchers(pkg.id, state.loanAmount)) {
                is Resource.Success -> _uiState.update { current ->
                    val stillValid = current.selectedVoucherId?.let { selected ->
                        result.data.any { it.id == selected }
                    } ?: true
                    current.copy(
                        applicableVouchers = result.data,
                        selectedVoucherId = if (stillValid) current.selectedVoucherId else null
                    )
                }
                is Resource.Error -> _uiState.update { it.copy(applicableVouchers = emptyList()) }
                Resource.Loading -> Unit
            }
        }
    }

    private fun requestQuote() {
        val state = _uiState.value
        val pkg = state.selectedPackage ?: return

        quoteJob?.cancel()
        quoteJob = viewModelScope.launch {
            _uiState.update { it.copy(isQuoteLoading = true) }
            val request = LoanQuoteRequest(
                loanAmount = state.loanAmount,
                tenorMonth = state.selectedTenorMonth.coerceAtLeast(1),
                hasInsurance = state.isInsuranceSelected,
                voucherId = state.selectedVoucherId
            )

            when (val result = loanRepository.quoteLoan(pkg.id, request)) {
                is Resource.Success -> _uiState.update {
                    val quote = result.data
                    it.copy(
                        quote = quote,
                        actualReceivedAmount = (quote.loanAmount - quote.insuranceFee).coerceAtLeast(0L),
                        insuranceFee = quote.insuranceFee,
                        interestAmount = quote.totalInterest,
                        monthlyPayment = quote.monthlyPayment,
                        totalPayment = quote.totalPayment,
                        discountAmount = quote.discountAmount,
                        finalInterestRate = quote.finalInterestRate,
                        voucherTitle = quote.voucherTitle,
                        isQuoteLoading = false,
                        errorMessage = null
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isQuoteLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }
}
