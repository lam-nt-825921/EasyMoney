package com.example.easymoney.ui.loan.information.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanApplicationRequest
import com.example.easymoney.domain.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmLoanInformationViewModel @Inject constructor(
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfirmLoanInformationUiState())
    val uiState: StateFlow<ConfirmLoanInformationUiState> = _uiState.asStateFlow()

    fun submitApplication(
        request: LoanApplicationRequest?,
        onSuccess: () -> Unit
    ) {
        if (request == null) {
            _uiState.update { it.copy(error = "Thiếu thông tin hồ sơ vay") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val requestWithMatchKey = ensureMatchKey(request)
            if (requestWithMatchKey == null) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        error = it.error ?: "Không thể xác thực eKYC cho gói vay này"
                    )
                }
                return@launch
            }

            when (val result = loanRepository.submitApplication(requestWithMatchKey)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSubmitting = false) }
                    onSuccess()
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    private suspend fun ensureMatchKey(request: LoanApplicationRequest): LoanApplicationRequest? {
        if (!request.ekycMatchKey.isNullOrBlank()) return request
        val packageId = request.packageId ?: return request
        return when (val result = loanRepository.matchEkyc(packageId)) {
            is Resource.Success -> {
                val match = result.data
                if (match.isMatched && match.canApplyLoan && !match.ekycMatchKey.isNullOrBlank()) {
                    request.copy(ekycMatchKey = match.ekycMatchKey)
                } else {
                    _uiState.update { it.copy(error = match.message) }
                    null
                }
            }
            is Resource.Error -> {
                _uiState.update { it.copy(error = result.message) }
                null
            }
            Resource.Loading -> null
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
