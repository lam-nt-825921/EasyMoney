package com.example.easymoney.ui.loan.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.ContractStatus
import com.example.easymoney.domain.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanManagementViewModel @Inject constructor(
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoanManagementUiState>(LoanManagementUiState.Loading)
    val uiState: StateFlow<LoanManagementUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = LoanManagementUiState.Loading
        viewModelScope.launch {
            when (val result = loanRepository.getApprovedContracts()) {
                is Resource.Success -> {
                    val active = result.data.filter { it.status == ContractStatus.APPROVED }
                    _uiState.value = if (active.isEmpty()) {
                        LoanManagementUiState.Empty
                    } else {
                        LoanManagementUiState.Success(active)
                    }
                }
                is Resource.Error -> _uiState.value = LoanManagementUiState.Error(result.message)
                is Resource.Loading -> Unit
            }
        }
    }

    fun cancelContract(contractId: String) {
        viewModelScope.launch {
            when (loanRepository.cancelContract(contractId)) {
                is Resource.Success -> load()
                is Resource.Error -> {
                    val state = _uiState.value
                    if (state is LoanManagementUiState.Success) {
                        _uiState.value = state.copy(pendingCancelId = null)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
