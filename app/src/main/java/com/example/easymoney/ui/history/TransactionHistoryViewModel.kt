package com.example.easymoney.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.TransactionGroup
import com.example.easymoney.domain.repository.TransactionHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionHistoryUiState(
    val isLoading: Boolean = true,
    val groups: List<TransactionGroup> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class TransactionHistoryViewModel @Inject constructor(
    private val repository: TransactionHistoryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionHistoryUiState())
    val state: StateFlow<TransactionHistoryUiState> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.getTransactionHistory()) {
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, groups = result.data, errorMessage = null)
                }
                is Resource.Error -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
                Resource.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }
}
