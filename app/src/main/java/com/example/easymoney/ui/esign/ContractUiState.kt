package com.example.easymoney.ui.esign

import com.example.easymoney.utils.UiText

/**
 * Workflow #88 — frontend-only state describing the OTP request lifecycle for the contract being
 * signed. Pressing `Ký hợp đồng` only calls the request-OTP endpoint when the current state allows
 * it ([NotRequested], [Expired], [Failed]); a valid [WaitingForOtp] reuses the existing request.
 */
sealed interface OtpRequestState {
    data object NotRequested : OtpRequestState
    data object Requesting : OtpRequestState
    data class WaitingForOtp(val expiresAtMillis: Long?) : OtpRequestState
    data object Expired : OtpRequestState
    data class Failed(val message: UiText?) : OtpRequestState
}

data class ContractUiState(
    val contractContent: String = "",
    val isLoading: Boolean = false,
    val isTermsAccepted: Boolean = false,
    // Workflow #69 — error messages routed through UiText for localised rendering.
    val errorMessage: UiText? = null,
    val isSigning: Boolean = false,
    val signSuccess: Boolean = false,

    // OTP States
    val showOtpDialog: Boolean = false,
    val otpError: UiText? = null,
    val isOtpVerifying: Boolean = false,
    val userPhone: String = "0913849582", // Mock phone as per design
    // Workflow #72 — contract id currently being signed.
    val loadedContractId: String? = null,
    val otpRequired: Boolean = true,

    // Workflow #87 — OTP from API/FCM is a SUGGESTION only; the user must explicitly tap to fill.
    // The typed value lives in [otpInput] so it survives dialog dismiss/reopen.
    val otpInput: String = "",
    val otpSuggestion: String? = null,
    val otpExpiresAt: Long? = null,
    // Workflow #88 — request-OTP lifecycle to prevent endpoint spam on repeated taps.
    val otpRequestState: OtpRequestState = OtpRequestState.NotRequested
) {
    /** A still-valid (non-expired, non-blank) OTP suggestion, or null when none should be offered. */
    fun validOtpSuggestion(): String? {
        val suggestion = otpSuggestion?.takeIf { it.isNotBlank() } ?: return null
        val expiry = otpExpiresAt ?: return suggestion
        return if (System.currentTimeMillis() <= expiry) suggestion else null
    }
}
