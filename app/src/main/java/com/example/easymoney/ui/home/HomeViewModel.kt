package com.example.easymoney.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.repository.HomeRepository
import com.example.easymoney.domain.repository.LoanRepository
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
    private val loanRepository: LoanRepository,
    private val homeRepository: HomeRepository,
    private val rewardRepository: RewardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    fun loadHomeData() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val userJob = async { loanRepository.getMyInfo() }
            val bannersJob = async { homeRepository.getBanners() }
            val hotLoansJob = async { homeRepository.getHotLoans() }
            val eKycJob = async { homeRepository.getEKycStatus() }
            val rewardsJob = async { rewardRepository.getRewardsCatalog() }

            val userRes = userJob.await()
            val bannersRes = bannersJob.await()
            val hotLoansRes = hotLoansJob.await()
            val eKycRes = eKycJob.await()
            val rewardsRes = rewardsJob.await()

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    userName = if (userRes is Resource.Success) userRes.data.fullName else state.userName,
                    banners = if (bannersRes is Resource.Success) bannersRes.data else state.banners,
                    hotLoans = if (hotLoansRes is Resource.Success) hotLoansRes.data else state.hotLoans,
                    eKycStatus = if (eKycRes is Resource.Success) eKycRes.data else state.eKycStatus,
                    rewardPoints = if (rewardsRes is Resource.Success) rewardsRes.data.totalPoints else state.rewardPoints,
                    errorMessage = if (userRes is Resource.Error) userRes.message else null
                )
            }
        }
    }
}
