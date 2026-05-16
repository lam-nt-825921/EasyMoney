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
    // Workflow #29 — extended filters
    val minInterest: Double? = null,
    val maxInterest: Double? = null,
    val hotOnly: Boolean = false,
    val newOnly: Boolean = false,
    val promotionalOnly: Boolean = false,
    val keyword: String = "",

    // Eligibility
    val eligibilityState: EligibilityUiState = EligibilityUiState.Idle
) {
    fun isAnyFilterActive(): Boolean =
        eligibleOnly || hotOnly || newOnly || promotionalOnly ||
            keyword.isNotBlank() || minInterest != null || maxInterest != null ||
            tenor != null || minAmount != null
}

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
                    // Workflow #29 — apply extended filters client-side over repo result.
                    // REMOTE mode sẽ chuyển filter sang query params (TODO trong LoanRepository).
                    val filtered = result.data.filter { pkg ->
                        (!state.hotOnly || pkg.isHot) &&
                            (!state.newOnly || pkg.isNew) &&
                            (!state.promotionalOnly || pkg.isPromotional) &&
                            (state.minInterest == null || pkg.interest >= state.minInterest) &&
                            (state.maxInterest == null || pkg.interest <= state.maxInterest) &&
                            (state.keyword.isBlank() ||
                                pkg.packageName.contains(state.keyword, ignoreCase = true))
                    }
                    _uiState.update { it.copy(packages = filtered, isLoading = false) }
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
        eligibleOnly: Boolean? = null,
        minInterest: Double? = null,
        maxInterest: Double? = null,
        hotOnly: Boolean? = null,
        newOnly: Boolean? = null,
        promotionalOnly: Boolean? = null,
        keyword: String? = null
    ) {
        _uiState.update { it.copy(
            minAmount = min ?: it.minAmount,
            maxAmount = max ?: it.maxAmount,
            tenor = tenor ?: it.tenor,
            eligibleOnly = eligibleOnly ?: it.eligibleOnly,
            minInterest = minInterest ?: it.minInterest,
            maxInterest = maxInterest ?: it.maxInterest,
            hotOnly = hotOnly ?: it.hotOnly,
            newOnly = newOnly ?: it.newOnly,
            promotionalOnly = promotionalOnly ?: it.promotionalOnly,
            keyword = keyword ?: it.keyword
        ) }
        loadPackages()
    }

    /** Workflow #29 — Reset all filters về mặc định. */
    fun resetFilters() {
        _uiState.update {
            it.copy(
                minAmount = null,
                maxAmount = null,
                tenor = null,
                eligibleOnly = false,
                minInterest = null,
                maxInterest = null,
                hotOnly = false,
                newOnly = false,
                promotionalOnly = false,
                keyword = ""
            )
        }
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
