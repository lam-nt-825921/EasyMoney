package com.example.easymoney.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.BalanceFlow
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.WalletInfo
import com.example.easymoney.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentUiState(
    val walletInfo: WalletInfo? = null,
    val cards: List<PaymentCard> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val walletRes = paymentRepository.getWalletInfo()
            val cardsRes = paymentRepository.getPaymentCards()
            
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    walletInfo = if (walletRes is Resource.Success) walletRes.data else state.walletInfo,
                    cards = if (cardsRes is Resource.Success) cardsRes.data else state.cards
                )
            }
        }
    }

    fun toggleAutoDeduction(enabled: Boolean) {
        viewModelScope.launch {
            paymentRepository.toggleAutoDeduction(enabled)
            loadData()
        }
    }
    
    fun topUp(amount: Long, cardId: String) {
        viewModelScope.launch {
            paymentRepository.topUp(amount, cardId)
            loadData()
        }
    }
}
