package com.example.easymoney.ui.account.changepassword

import androidx.annotation.StringRes

/**
 * UI state for the change-password screen. [validationError]/[submitError] hold
 * string resource ids so the UI layer owns all user-facing text.
 */
data class ChangePasswordUiState(
    val oldPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    @StringRes val validationError: Int? = null,
    val submitError: String? = null
) {
    val canSubmit: Boolean
        get() = !isSubmitting &&
            oldPassword.isNotBlank() &&
            newPassword.isNotBlank() &&
            confirmPassword.isNotBlank()
}
