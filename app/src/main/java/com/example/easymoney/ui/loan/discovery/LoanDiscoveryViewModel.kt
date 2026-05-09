package com.example.easymoney.ui.loan.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanPackageModel
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.ui.home.EligibilityUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoanDiscoveryUiState(
    val packages: List<LoanPackageModel> = emptyList(),
    val selectedPackage: LoanPackageModel? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    
    // Filters
    val minAmount: Long? = null,
    val maxAmount: Long? = null,
    val tenor: Int? = null,
    val eligibleOnly: Boolean = false,
    
    // Eligibility
    val eligibilityState: EligibilityUiState = EligibilityUiState.Idle
)

@HiltViewModel
class LoanDiscoveryViewModel @Inject constructor(
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoanDiscoveryUiState())
    val uiState: StateFlow<LoanDiscoveryUiState> = _uiState.asStateFlow()

    fun loadPackages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val state = _uiState.value
            when (val result = loanRepository.getLoanPackages(
                minAmount = state.minAmount,
                maxAmount = state.maxAmount,
                tenor = state.tenor,
                eligibleOnly = state.eligibleOnly
            )) {
                is Resource.Success -> {
                    _uiState.update { it.copy(packages = result.data, isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun updateFilters(
        min: Long? = null,
        max: Long? = null,
        tenor: Int? = null,
        eligibleOnly: Boolean? = null
    ) {
        _uiState.update { it.copy(
            minAmount = min ?: it.minAmount,
            maxAmount = max ?: it.maxAmount,
            tenor = tenor ?: it.tenor,
            eligibleOnly = eligibleOnly ?: it.eligibleOnly
        ) }
        loadPackages()
    }

    fun loadPackageDetail(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = loanRepository.getLoanPackageById(id)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(selectedPackage = result.data, isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun checkEligibility(packageId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(eligibilityState = EligibilityUiState.Checking) }
            when (val result = loanRepository.checkEligibility(packageId)) {
                is Resource.Success -> {
                    val eligibility = result.data
                    if (eligibility.isEligible) {
                        _uiState.update { it.copy(eligibilityState = EligibilityUiState.Success(packageId)) }
                    } else {
                        when (eligibility.action) {
                            "NAVIGATE_PROFILE" -> {
                                _uiState.update { it.copy(eligibilityState = EligibilityUiState.MissingInfo(eligibility.message ?: "")) }
                            }
                            "SHOW_REJECT" -> {
                                _uiState.update { it.copy(eligibilityState = EligibilityUiState.Rejected(eligibility.message ?: "")) }
                            }
                            else -> {
                                _uiState.update { it.copy(eligibilityState = EligibilityUiState.Error("Unknown error")) }
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(eligibilityState = EligibilityUiState.Error(result.message ?: "Network error")) }
                }
                else -> {}
            }
        }
    }

    fun resetEligibilityState() {
        _uiState.update { it.copy(eligibilityState = EligibilityUiState.Idle) }
    }
}
