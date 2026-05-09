package com.example.easymoney.ui.security

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SecurityUiState(
    val isBiometricEnabled: Boolean = false,
    val isBiometricSupported: Boolean = true // Mocked
)

@HiltViewModel
class SecurityViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    fun toggleBiometric(enabled: Boolean) {
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }
}
