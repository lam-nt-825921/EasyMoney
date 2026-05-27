package com.example.easymoney.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.repository.HomeRepository
import com.example.easymoney.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.easymoney.domain.repository.RewardRepository

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val rewardRepository: RewardRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        userRepository.getCachedProfileCompletion()?.let { cachedCompletion ->
            _uiState.update { it.copy(profileCompletion = cachedCompletion) }
        }
        loadHomeData()
    }

    fun loadHomeData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val userJob = async { userRepository.getProfile() }
            val bannersJob = async { homeRepository.getBanners() }
            val hotLoansJob = async { homeRepository.getHotLoans() }
            val recommendedLoanJob = async { homeRepository.getRecommendedLoan() }
            val eKycJob = async { homeRepository.getEKycStatus() }
            val profileCompletionJob = async { userRepository.getProfileCompletion() }
            val rewardsJob = async { rewardRepository.getRewardsCatalog() }

            val userRes = userJob.await()
            val bannersRes = bannersJob.await()
            val hotLoansRes = hotLoansJob.await()
            val recommendedLoanRes = recommendedLoanJob.await()
            val eKycRes = eKycJob.await()
            val profileCompletionRes = profileCompletionJob.await()
            val rewardsRes = rewardsJob.await()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    userName = if (userRes is Resource.Success) {
                        userRes.data.personalInfo.fullName.ifBlank { state.userName }
                    } else {
                        state.userName
                    },
                    banners = if (bannersRes is Resource.Success) bannersRes.data else state.banners,
                    hotLoans = if (hotLoansRes is Resource.Success) hotLoansRes.data else state.hotLoans,
                    recommendedLoan = if (recommendedLoanRes is Resource.Success) recommendedLoanRes.data else state.recommendedLoan,
                    eKycStatus = if (eKycRes is Resource.Success) eKycRes.data else state.eKycStatus,
                    profileCompletion = when (profileCompletionRes) {
                        is Resource.Success -> profileCompletionRes.data
                        is Resource.Error -> profileCompletionRes.data ?: state.profileCompletion
                        else -> state.profileCompletion
                    },
                    profileCompletionErrorMessage = if (profileCompletionRes is Resource.Error) profileCompletionRes.message else null,
                    rewardPoints = if (rewardsRes is Resource.Success) rewardsRes.data.totalPoints else state.rewardPoints,
                    errorMessage = if (userRes is Resource.Error) userRes.message else null
                )
            }
        }
    }

    fun refreshProfileCompletion() {
        viewModelScope.launch {
            when (val result = userRepository.getProfileCompletion(forceRefresh = true)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            profileCompletion = result.data,
                            profileCompletionErrorMessage = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            profileCompletion = result.data ?: it.profileCompletion,
                            profileCompletionErrorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }
}
