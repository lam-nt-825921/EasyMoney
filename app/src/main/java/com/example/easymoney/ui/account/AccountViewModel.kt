package com.example.easymoney.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.repository.HomeRepository
import com.example.easymoney.domain.repository.RewardRepository
import com.example.easymoney.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiState(
    val isLoading: Boolean = true,
    val fullName: String = "",
    val phoneNumber: String = "",
    // Workflow #57 — reward points loaded from backend, not hard-coded.
    val rewardPoints: Int = 0,
    val isRewardLoading: Boolean = false,
    val supportUrl: String? = null,
    val supportTitle: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val homeRepository: HomeRepository,
    private val rewardRepository: RewardRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    init {
        loadProfile()
        loadRewardPoints()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = userRepository.getProfile()) {
                is Resource.Success -> {
                    val info = result.data.personalInfo
                    _state.update {
                        it.copy(
                            isLoading = false,
                            fullName = info.fullName,
                            phoneNumber = info.phoneNumber
                        )
                    }
                }
                is Resource.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun loadRewardPoints() {
        viewModelScope.launch {
            _state.update { it.copy(isRewardLoading = true) }
            when (val result = rewardRepository.getRewardsCatalog()) {
                is Resource.Success -> _state.update {
                    it.copy(isRewardLoading = false, rewardPoints = result.data.totalPoints)
                }
                // Reward failure must not block account screen — leave previous points value.
                is Resource.Error -> _state.update { it.copy(isRewardLoading = false) }
                Resource.Loading -> Unit
            }
        }
    }

    fun openCustomerSupport() {
        viewModelScope.launch {
            when (val result = homeRepository.getCustomerSupportLink()) {
                is Resource.Success -> _state.update {
                    it.copy(
                        supportUrl = result.data.url,
                        supportTitle = result.data.title,
                        errorMessage = null
                    )
                }
                is Resource.Error -> _state.update { it.copy(errorMessage = result.message) }
                Resource.Loading -> Unit
            }
        }
    }

    fun consumeSupportNavigation() {
        _state.update { it.copy(supportUrl = null, supportTitle = null) }
    }
}
