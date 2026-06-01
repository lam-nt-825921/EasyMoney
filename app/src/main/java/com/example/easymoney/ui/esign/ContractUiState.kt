package com.example.easymoney.ui.esign

data class ContractUiState(
    val contractContent: String = "",
    val isLoading: Boolean = false,
    val isTermsAccepted: Boolean = false,
    val errorMessage: String? = null,
    val isSigning: Boolean = false,
    val signSuccess: Boolean = false,
    
    // OTP States
    val showOtpDialog: Boolean = false,
    val otpError: String? = null,
    val isOtpVerifying: Boolean = false,
    val userPhone: String = "0913849582", // Mock phone as per design
    // Workflow #64 — biometric 2FA local toggle; OTP chỉ mở sau khi gate pass.
    val is2FAEnabled: Boolean = false
)
