package com.example.easymoney.ui.security

import android.app.Application
import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModel
import com.example.easymoney.data.local.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SecurityUiState(
    val isBiometricEnabled: Boolean = false,
    val isBiometricSupported: Boolean = false
)

@HiltViewModel
class SecurityViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SecurityUiState(
            isBiometricEnabled = appPreferences.isBiometric2FAEnabled,
            isBiometricSupported = isBiometricAvailable(application)
        )
    )
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    /** Workflow #34 — gọi sau khi `BiometricPrompt` xác thực thành công ở UI layer. */
    fun toggleBiometric(enabled: Boolean) {
        if (enabled && !_uiState.value.isBiometricSupported) return
        appPreferences.isBiometric2FAEnabled = enabled
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }

    private fun isBiometricAvailable(application: Application): Boolean {
        val manager = BiometricManager.from(application)
        return manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }
}
