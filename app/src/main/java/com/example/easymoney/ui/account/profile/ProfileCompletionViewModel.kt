package com.example.easymoney.ui.account.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.UserProfile
import com.example.easymoney.domain.repository.UserRepository
import com.example.easymoney.ui.common.identity.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileCompletionUiState(
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeModule: IdentityModule? = null
)

enum class IdentityModule {
    FACE_CAPTURE,
    NFC_READER,
    BIOMETRIC,
    DOCUMENT_UPLOAD
}

@HiltViewModel
class ProfileCompletionViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileCompletionUiState())
    val uiState: StateFlow<ProfileCompletionUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = userRepository.getProfile()) {
                is Resource.Success -> {
                    _uiState.update { it.copy(profile = result.data, isLoading = false) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                else -> {}
            }
        }
    }

    fun openModule(module: IdentityModule) {
        _uiState.update { it.copy(activeModule = module) }
    }

    fun closeModule() {
        _uiState.update { it.copy(activeModule = null) }
    }

    fun onFaceCaptureResult(result: FaceCaptureResult) {
        if (result.livenessVerified) {
            updateIdentityStatus { it.copy(isFaceVerified = true) }
        }
        closeModule()
    }

    fun onNfcResult(result: NfcResult) {
        if (result.isSuccess) {
            updateIdentityStatus { it.copy(isNfcVerified = true) }
            // Update personal info from NFC raw data if needed
        }
        closeModule()
    }

    fun onBiometricResult(result: BiometricResult) {
        if (result.isSuccess) {
            updateIdentityStatus { it.copy(isBiometricEnabled = true) }
        }
        closeModule()
    }

    private fun updateIdentityStatus(update: (com.example.easymoney.domain.model.IdentityVerificationStatus) -> com.example.easymoney.domain.model.IdentityVerificationStatus) {
        val currentProfile = _uiState.value.profile
        val newStatus = update(currentProfile.identityStatus)
        val updatedProfile = currentProfile.copy(identityStatus = newStatus)
        
        viewModelScope.launch {
            userRepository.updateProfile(updatedProfile)
            _uiState.update { it.copy(profile = updatedProfile) }
        }
    }
}
