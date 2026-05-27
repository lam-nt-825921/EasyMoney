package com.example.easymoney.ui.reward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.RewardCatalogItem
import com.example.easymoney.domain.model.UserRewardVoucher
import com.example.easymoney.domain.repository.RewardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias RewardItem = RewardCatalogItem

data class RewardUiState(
    val totalPoints: Int = 1250,
    val rewards: List<RewardItem> = emptyList(),
    val redeemedRewards: List<UserRewardVoucher> = emptyList(),
    val isLoading: Boolean = false,
    val pendingConfirmId: String? = null,
    val isRedeeming: Boolean = false,
    val redeemSuccessMessage: String? = null,
    val latestRedeemedVoucher: UserRewardVoucher? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class RewardViewModel @Inject constructor(
    private val rewardRepository: RewardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RewardUiState())
    val uiState: StateFlow<RewardUiState> = _uiState.asStateFlow()

    init {
        loadRewards()
    }

    private fun loadRewards() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val userResult = rewardRepository.getRewardsCatalog()) {
                is Resource.Success -> _uiState.update {
                    it.copy(totalPoints = userResult.data.totalPoints, redeemedRewards = userResult.data.vouchers)
                }
                is Resource.Error -> _uiState.update { it.copy(errorMessage = userResult.message) }
                is Resource.Loading -> Unit
            }

            when (val result = rewardRepository.getRewardCatalogItems()) {
                is Resource.Success -> _uiState.update {
                    it.copy(rewards = result.data, isLoading = false)
                }
                is Resource.Error -> _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun onRedeemRequest(itemId: String) {
        _uiState.update { it.copy(pendingConfirmId = itemId) }
    }

    fun onCancelRedeem() {
        _uiState.update { it.copy(pendingConfirmId = null) }
    }

    fun onConfirmRedeem() {
        val state = _uiState.value
        val id = state.pendingConfirmId ?: return
        val item = state.rewards.firstOrNull { it.id == id } ?: return
        if (state.totalPoints < item.points) {
            _uiState.update { it.copy(errorMessage = "Không đủ điểm để đổi", pendingConfirmId = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isRedeeming = true) }
            when (val res = rewardRepository.redeemReward(id)) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        isRedeeming = false,
                        pendingConfirmId = null,
                        totalPoints = res.data.totalPoints,
                        redeemedRewards = listOf(res.data.voucher) + it.redeemedRewards,
                        latestRedeemedVoucher = res.data.voucher,
                        redeemSuccessMessage = "Đổi thành công: ${res.data.voucher.title}"
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isRedeeming = false, pendingConfirmId = null, errorMessage = res.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun consumeMessages() {
        _uiState.update { it.copy(redeemSuccessMessage = null, latestRedeemedVoucher = null, errorMessage = null) }
    }
}
