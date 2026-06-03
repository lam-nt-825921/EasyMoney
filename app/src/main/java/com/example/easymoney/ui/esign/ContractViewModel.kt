package com.example.easymoney.ui.esign

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.appcompat.app.AppCompatDelegate
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.messaging.ContractOtpHolder
import com.example.easymoney.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ContractViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val contractOtpHolder: ContractOtpHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContractUiState())
    val uiState: StateFlow<ContractUiState> = _uiState.asStateFlow()

    init {
        // Workflow #72 — auto-fill the OTP field when an FCM CONTRACT_SIGN_OTP arrives for the
        // contract being signed. Only the matching contract id is honoured; the user still confirms.
        viewModelScope.launch {
            contractOtpHolder.latest.collect { pending ->
                val currentId = _uiState.value.loadedContractId ?: return@collect
                if (pending != null && pending.contractId == currentId) {
                    _uiState.update { it.copy(otpAutofill = pending.otp) }
                }
            }
        }
    }

    fun loadContract(loanId: String) {
        if (loanId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, loadedContractId = loanId) }
            // Workflow #72 — prefer the canonical detail endpoint; fall back to the legacy
            // content endpoint if it is unavailable so existing flows keep working.
            when (val detail = loanRepository.getContractDetail(loanId)) {
                is Resource.Success -> {
                    val data = detail.data
                    _uiState.update { it.copy(
                        contractContent = data.content,
                        otpRequired = data.otpRequired,
                        isLoading = false
                    ) }
                }
                is Resource.Error -> loadContractContentFallback(loanId)
                is Resource.Loading -> Unit
            }
            // Apply any OTP that already arrived for this contract before the screen opened.
            contractOtpHolder.otpFor(loanId)?.let { otp ->
                _uiState.update { it.copy(otpAutofill = otp) }
            }
        }
    }

    private suspend fun loadContractContentFallback(loanId: String) {
        when (val result = loanRepository.getContractContent(loanId, currentLanguageTag())) {
            is Resource.Success -> _uiState.update {
                it.copy(contractContent = result.data ?: "", isLoading = false)
            }
            is Resource.Error -> _uiState.update {
                it.copy(errorMessage = UiText.DynamicString(result.message), isLoading = false)
            }
            is Resource.Loading -> Unit
        }
    }

    fun onTermsAcceptedChange(accepted: Boolean) {
        _uiState.update { it.copy(isTermsAccepted = accepted) }
    }

    fun showOtpDialog() {
        _uiState.update { it.copy(showOtpDialog = true, otpError = null) }
        requestSignOtp()
        // Surface an OTP that may already have arrived via FCM for this contract.
        _uiState.value.loadedContractId?.let { id ->
            contractOtpHolder.otpFor(id)?.let { otp ->
                _uiState.update { it.copy(otpAutofill = otp) }
            }
        }
    }

    fun hideOtpDialog() {
        _uiState.update { it.copy(showOtpDialog = false, otpError = null) }
    }

    private fun requestSignOtp() {
        val contractId = _uiState.value.loadedContractId ?: return
        viewModelScope.launch {
            // Workflow #81 — autofill OTP ngay từ response của request-otp (ngoài kênh FCM),
            // và lưu vào holder để khớp đúng contract id. Người dùng vẫn tự bấm xác nhận.
            when (val result = loanRepository.requestSignOtp(contractId)) {
                is Resource.Success -> {
                    val otp = result.data.otp
                    if (!otp.isNullOrBlank()) {
                        contractOtpHolder.submit(contractId, otp, result.data.expiresAt)
                        _uiState.update { it.copy(otpAutofill = otp) }
                    }
                }
                is Resource.Error, is Resource.Loading -> Unit
            }
        }
    }

    fun verifyOtp(otp: String, contractId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOtpVerifying = true, otpError = null) }
            // Workflow #81 — final sign gửi kèm OTP đã autofill (body { otp, purpose }).
            when (val signResult = loanRepository.signContract(contractId, otp)) {
                is Resource.Success -> {
                    contractOtpHolder.consume(contractId)
                    _uiState.update { it.copy(isOtpVerifying = false, showOtpDialog = false, signSuccess = true) }
                    onSuccess()
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(isOtpVerifying = false, otpError = UiText.DynamicString(signResult.message))
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun resendOtp() {
        requestSignOtp()
    }

    fun signContract(onSuccess: () -> Unit) {
        // Thay vì ký trực tiếp, bây giờ hiển thị OTP
        showOtpDialog()
    }

    private fun currentLanguageTag(): String {
        return AppCompatDelegate.getApplicationLocales()[0]?.language
            ?: Locale.getDefault().language
            ?: "vi"
    }
}
