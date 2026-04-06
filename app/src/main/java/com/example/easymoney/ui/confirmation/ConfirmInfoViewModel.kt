package com.example.easymoney.ui.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmInfoViewModel @Inject constructor(
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmInfoUiState())
    val uiState: StateFlow<ConfirmInfoUiState> = _uiState.asStateFlow()

    init {
        loadMyInfo()
    }

    fun loadMyInfo() {
        _uiState.update {
            val nextLoadState = if (it.userInfo == null) {
                ConfirmInfoLoadState.InitialLoading
            } else {
                ConfirmInfoLoadState.Loading
            }
            it.copy(loadState = nextLoadState)
        }

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
                    _uiState.update {
                        val nextLoadState = if (it.userInfo == null) {
                            ConfirmInfoLoadState.InitialLoading
                        } else {
                            ConfirmInfoLoadState.Loading
                        }
                        it.copy(loadState = nextLoadState)
                    }
                }
            }
        }
    }
}

