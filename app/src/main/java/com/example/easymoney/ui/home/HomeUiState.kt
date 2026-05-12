package com.example.easymoney.ui.home

import com.example.easymoney.domain.model.Banner
import com.example.easymoney.domain.model.LoanProduct
import com.example.easymoney.domain.model.EKycStatus

sealed interface EligibilityUiState {
    data object Idle : EligibilityUiState
    data object Checking : EligibilityUiState
    data class Success(val packageId: String, val skipDetail: Boolean = false) : EligibilityUiState
    data class MissingInfo(val message: String) : EligibilityUiState
    data class Rejected(val message: String) : EligibilityUiState
    data class Error(val message: String) : EligibilityUiState
}

data class HomeUiState(
    val userName: String = "",
    val banners: List<Banner> = emptyList(),
    val hotLoans: List<LoanProduct> = emptyList(),
    val eKycStatus: EKycStatus? = null,
    val rewardPoints: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val eligibilityState: EligibilityUiState = EligibilityUiState.Idle
)
