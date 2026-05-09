package com.example.easymoney.ui.reward

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RewardItem(
    val id: String,
    val title: String,
    val points: Int,
    val description: String,
    val category: String,
    val imageUrl: String? = null
)

data class RewardUiState(
    val totalPoints: Int = 1250,
    val rewards: List<RewardItem> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class RewardViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RewardUiState())
    val uiState: StateFlow<RewardUiState> = _uiState.asStateFlow()

    init {
        loadRewards()
    }

    private fun loadRewards() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(1000)
            val mockRewards = listOf(
                RewardItem("1", "Voucher giảm 1% lãi suất", 500, "Áp dụng cho khoản vay tiêu dùng", "Tài chính"),
                RewardItem("2", "Voucher giảm 50k phí dịch vụ", 200, "Áp dụng cho mọi khoản vay", "Tài chính"),
                RewardItem("3", "Thẻ cào 50k", 500, "Mã thẻ Viettel/Vinaphone", "Viễn thông"),
                RewardItem("4", "Hoàn tiền 100k", 1000, "Cộng trực tiếp vào số dư ví", "Cashback")
            )
            _uiState.update { it.copy(rewards = mockRewards, isLoading = false) }
        }
    }
}
