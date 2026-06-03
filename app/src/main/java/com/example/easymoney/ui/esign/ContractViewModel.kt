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
        // Workflow #87 — an FCM CONTRACT_SIGN_OTP for the contract being signed is stored as a
        // SUGGESTION only. The six OTP boxes are never filled automatically; the user must tap
        // "Điền OTP từ thông báo" to copy it into the input.
        viewModelScope.launch {
            contractOtpHolder.latest.collect { pending ->
                val currentId = _uiState.value.loadedContractId ?: return@collect
                if (pending != null && pending.contractId == currentId) {
                    _uiState.update { state ->
                        state.copy(
                            otpSuggestion = pending.otp,
                            otpExpiresAt = pending.expiresAt,
                            // A suggestion arriving means the OTP we requested is now available.
                            otpRequestState = when (state.otpRequestState) {
                                is OtpRequestState.Requesting,
                                is OtpRequestState.NotRequested ->
                                    OtpRequestState.WaitingForOtp(pending.expiresAt)
                                else -> state.otpRequestState
                            }
                        )
                    }
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
            // Workflow #87 — surface any OTP already received for this contract as a suggestion.
            contractOtpHolder.otpFor(loanId)?.let { otp ->
                _uiState.update { it.copy(otpSuggestion = otp) }
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

    /** Workflow #87 — controlled OTP input; only digits, max 6. */
    fun onOtpInputChange(value: String) {
        val sanitized = value.filter { it.isDigit() }.take(6)
        _uiState.update { it.copy(otpInput = sanitized, otpError = null) }
    }

    /** Workflow #87 — copy the suggested OTP into the input only when the user explicitly asks. */
    fun fillOtpSuggestion() {
        val suggestion = _uiState.value.validOtpSuggestion() ?: return
        _uiState.update { it.copy(otpInput = suggestion.filter { c -> c.isDigit() }.take(6), otpError = null) }
    }

    fun showOtpDialog() {
        _uiState.update { it.copy(showOtpDialog = true, otpError = null) }
        // Workflow #88 — only call request-OTP when there is no valid outstanding request.
        if (shouldRequestOtp()) {
            requestSignOtp()
        }
    }

    private fun shouldRequestOtp(): Boolean = when (val state = _uiState.value.otpRequestState) {
        is OtpRequestState.NotRequested,
        is OtpRequestState.Expired,
        is OtpRequestState.Failed -> true
        // Re-request only if the previously requested OTP has already expired.
        is OtpRequestState.WaitingForOtp -> isExpired(state.expiresAtMillis)
        is OtpRequestState.Requesting -> false
    }

    private fun isExpired(expiresAtMillis: Long?): Boolean =
        expiresAtMillis != null && System.currentTimeMillis() > expiresAtMillis

    fun hideOtpDialog() {
        // Workflow #88 — dismissing only hides the dialog; request state, typed value, and the
        // suggestion are preserved so reopening does not trigger another request-OTP call.
        _uiState.update { it.copy(showOtpDialog = false) }
    }

    private fun requestSignOtp() {
        val contractId = _uiState.value.loadedContractId ?: return
        if (_uiState.value.otpRequestState is OtpRequestState.Requesting) return
        _uiState.update { it.copy(otpRequestState = OtpRequestState.Requesting) }
        viewModelScope.launch {
            when (val result = loanRepository.requestSignOtp(contractId)) {
                is Resource.Success -> {
                    val otp = result.data.otp
                    val expiresAt = result.data.expiresAt
                    // Workflow #87 — store as suggestion + holder; never write into the input field.
                    if (!otp.isNullOrBlank()) {
                        contractOtpHolder.submit(contractId, otp, expiresAt)
                    }
                    _uiState.update { state ->
                        state.copy(
                            otpRequestState = OtpRequestState.WaitingForOtp(expiresAt),
                            otpSuggestion = otp?.takeIf { it.isNotBlank() } ?: state.otpSuggestion,
                            otpExpiresAt = expiresAt ?: state.otpExpiresAt
                        )
                    }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(otpRequestState = OtpRequestState.Failed(UiText.DynamicString(result.message)))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun verifyOtp(otp: String, contractId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isOtpVerifying = true, otpError = null) }
            // Workflow #81/#88 — final sign sends the body { otp, purpose: "SIGN_CONTRACT" }.
            when (val signResult = loanRepository.signContract(contractId, otp)) {
                is Resource.Success -> {
                    // Workflow #88 — consume the OTP holder + reset OTP state after a successful sign.
                    contractOtpHolder.consume(contractId)
                    _uiState.update {
                        it.copy(
                            isOtpVerifying = false,
                            showOtpDialog = false,
                            signSuccess = true,
                            otpInput = "",
                            otpSuggestion = null,
                            otpExpiresAt = null,
                            otpRequestState = OtpRequestState.NotRequested
                        )
                    }
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
        // Workflow #88 — explicit user retry: reset state and request a fresh OTP.
        _uiState.update { it.copy(otpInput = "", otpRequestState = OtpRequestState.NotRequested) }
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
