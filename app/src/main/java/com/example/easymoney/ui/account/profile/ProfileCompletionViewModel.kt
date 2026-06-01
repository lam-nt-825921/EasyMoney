package com.example.easymoney.ui.account.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.R
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.UserProfile
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.domain.repository.UserRepository
import com.example.easymoney.ui.common.identity.*
import com.example.easymoney.utils.UiText
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
    // Workflow #65 — error now uses UiText so NFC/profile errors render localised copy.
    val errorMessage: UiText? = null,
    val activeModule: IdentityModule? = null,
    val ekycSessionId: String? = null,
    val isSubmittingIdentity: Boolean = false
)

enum class IdentityModule {
    FACE_CAPTURE,
    NFC_READER,
    BIOMETRIC,
    DOCUMENT_UPLOAD
}

@HiltViewModel
class ProfileCompletionViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val loanRepository: LoanRepository
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
                    _uiState.update {
                        it.copy(errorMessage = UiText.DynamicString(result.message), isLoading = false)
                    }
                }
                else -> {}
            }
        }
    }

    fun openModule(module: IdentityModule, supportsNfc: Boolean = false) {
        if (module == IdentityModule.NFC_READER || module == IdentityModule.DOCUMENT_UPLOAD || module == IdentityModule.FACE_CAPTURE) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSubmittingIdentity = true, errorMessage = null) }
                val currentSession = _uiState.value.ekycSessionId
                val sessionId = if (currentSession.isNullOrBlank()) {
                    when (val result = loanRepository.startEkycSession(supportsNfc)) {
                        is Resource.Success -> result.data
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isSubmittingIdentity = false,
                                    errorMessage = UiText.DynamicString(result.message)
                                )
                            }
                            return@launch
                        }
                        Resource.Loading -> null
                    }
                } else {
                    currentSession
                }
                _uiState.update {
                    it.copy(
                        activeModule = module,
                        ekycSessionId = sessionId,
                        isSubmittingIdentity = false
                    )
                }
            }
        } else {
            _uiState.update { it.copy(activeModule = module) }
        }
    }

    fun closeModule() {
        _uiState.update { it.copy(activeModule = null) }
    }

    fun onFaceCaptureUploaded() {
        closeModule()
        refreshAfterIdentityVerification()
    }

    fun onIdentityError(message: String) {
        _uiState.update {
            it.copy(errorMessage = UiText.DynamicString(message), isSubmittingIdentity = false)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onNfcResult(result: NfcResult) {
        if (result.isSuccess) {
            val nfcData = result.extractedInfo
            val missingRequiredFields = listOf("national_id", "full_name", "date_of_birth")
                .filter { nfcData[it].isNullOrBlank() }

            if (missingRequiredFields.isNotEmpty()) {
                _uiState.update {
                    it.copy(
                        errorMessage = UiText.StringResource(R.string.profile_nfc_error_missing_data),
                        isSubmittingIdentity = false
                    )
                }
                closeModule()
                return
            }

            viewModelScope.launch {
                _uiState.update { it.copy(isSubmittingIdentity = true, errorMessage = null) }
                when (val submitResult = loanRepository.submitNfcIdentity(_uiState.value.ekycSessionId, nfcData)) {
                    is Resource.Success -> refreshAfterIdentityVerification()
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isSubmittingIdentity = false,
                            errorMessage = UiText.DynamicString(submitResult.message)
                        )
                    }
                    Resource.Loading -> Unit
                }
            }
        } else {
            _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.profile_nfc_error_read_failed))
            }
        }
        closeModule()
    }

    fun onBiometricResult(result: BiometricResult) {
        if (result.isSuccess) {
            updateIdentityStatus { it.copy(isBiometricEnabled = true) }
        }
        closeModule()
    }

    fun onDocumentUploadResult(result: DocumentResult) {
        if (result.fileUri != null || result.isFromCamera) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSubmittingIdentity = true, errorMessage = null) }
                when (val uploadResult = loanRepository.uploadIdentityDocument()) {
                    is Resource.Success -> refreshAfterIdentityVerification()
                    is Resource.Error -> _uiState.update {
                        it.copy(
                            isSubmittingIdentity = false,
                            errorMessage = UiText.DynamicString(uploadResult.message)
                        )
                    }
                    Resource.Loading -> Unit
                }
            }
        }
        closeModule()
    }

    fun updateAvatar(uri: String) {
        val updatedProfile = _uiState.value.profile.copy(avatarUri = uri)
        viewModelScope.launch {
            val result = userRepository.updateProfile(updatedProfile)
            if (result is Resource.Success) {
                userRepository.getProfileCompletion(forceRefresh = true)
                _uiState.update { it.copy(profile = updatedProfile) }
            }
        }
    }

    private fun updateIdentityStatus(update: (com.example.easymoney.domain.model.IdentityVerificationStatus) -> com.example.easymoney.domain.model.IdentityVerificationStatus) {
        val currentProfile = _uiState.value.profile
        val newStatus = update(currentProfile.identityStatus)
        val updatedProfile = currentProfile.copy(identityStatus = newStatus)

        viewModelScope.launch {
            val result = userRepository.updateProfile(updatedProfile)
            if (result is Resource.Success) {
                userRepository.getProfileCompletion(forceRefresh = true)
                _uiState.update { it.copy(profile = updatedProfile) }
            }
        }
    }

    private fun refreshAfterIdentityVerification() {
        viewModelScope.launch {
            userRepository.getProfileCompletion(forceRefresh = true)
            when (val profileResult = userRepository.getProfile()) {
                is Resource.Success -> _uiState.update {
                    it.copy(
                        profile = profileResult.data,
                        isSubmittingIdentity = false,
                        errorMessage = null,
                        activeModule = null
                    )
                }
                is Resource.Error -> _uiState.update {
                    it.copy(
                        isSubmittingIdentity = false,
                        errorMessage = UiText.DynamicString(profileResult.message),
                        activeModule = null
                    )
                }
                Resource.Loading -> Unit
            }
        }
    }
}
