package com.example.easymoney.ui.esign

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
class ContractViewModel @Inject constructor(
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContractUiState())
    val uiState: StateFlow<ContractUiState> = _uiState.asStateFlow()

    fun loadContract(loanId: String) {
        if (loanId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = loanRepository.getContractContent(loanId)) {
                is Resource.Success -> {
                    _uiState.update { it.copy(
                        contractContent = result.data ?: "",
                        isLoading = false
                    ) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(
                        errorMessage = result.message,
                        isLoading = false
                    ) }
                }
                else -> {}
            }
        }
    }

    fun onTermsAcceptedChange(accepted: Boolean) {
        _uiState.update { it.copy(isTermsAccepted = accepted) }
    }

    fun showOtpDialog() {
        _uiState.update { it.copy(showOtpDialog = true, otpError = null) }
        // Backend tự xác định SĐT từ token/session
        viewModelScope.launch {
            loanRepository.sendOtp("esign")
        }
    }

    fun hideOtpDialog() {
        _uiState.update { it.copy(showOtpDialog = false, otpError = null) }
    }

    fun verifyOtp(otp: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOtpVerifying = true, otpError = null) }
            val result = loanRepository.verifyOtp(otp)
            
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isOtpVerifying = false, showOtpDialog = false, signSuccess = true) }
                    onSuccess()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isOtpVerifying = false, otpError = result.message) }
                }
                else -> {}
            }
        }
    }

    fun resendOtp() {
        viewModelScope.launch {
            loanRepository.sendOtp("esign")
        }
    }

    fun signContract(onSuccess: () -> Unit) {
        // Thay vì ký trực tiếp, bây giờ hiển thị OTP
        showOtpDialog()
    }
}
