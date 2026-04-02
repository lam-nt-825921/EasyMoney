package com.example.easymoney.ui.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmInfoViewModel @Inject constructor(
    private val loanRepository: LoanRepository
) : ViewModel() {

    sealed interface NavigationEvent {
        data object ToLoanInformation : NavigationEvent
        data object ToEditInformation : NavigationEvent
    }

    private val _uiState = MutableStateFlow(ConfirmInfoUiState())
    val uiState: StateFlow<ConfirmInfoUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent: SharedFlow<NavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        loadMyInfo()
    }

    fun loadMyInfo() {
        _uiState.update { it.copy(loadState = ConfirmInfoLoadState.Loading) }

        viewModelScope.launch {
            when (val result = loanRepository.getMyInfo()) {
                is Resource.Success -> {
                    val myInfo = result.data
                    _uiState.update {
                        it.copy(
                            userInfo = myInfo,
                            loadState = ConfirmInfoLoadState.Success
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            loadState = ConfirmInfoLoadState.Error(
                                result.message.ifBlank { "Khong the tai du lieu" }
                            )
                        )
                    }
                }

                is Resource.Loading -> {
                    _uiState.update { it.copy(loadState = ConfirmInfoLoadState.Loading) }
                }
            }
        }
    }

    fun onContinueClick() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.ToLoanInformation)
        }
    }

    fun onEditInfoClick() {
        viewModelScope.launch {
            _navigationEvent.emit(NavigationEvent.ToEditInformation)
        }
    }
}

