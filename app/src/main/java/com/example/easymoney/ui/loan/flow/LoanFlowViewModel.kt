package com.example.easymoney.ui.loan.flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanApplicationRequest
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanFlowViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoanFlowModel())
    val uiState: StateFlow<LoanFlowModel> = _uiState.asStateFlow()

    init {
        preFillFromProfile()
    }

    private fun preFillFromProfile() {
        viewModelScope.launch {
            when (val result = userRepository.getProfile()) {
                is Resource.Success -> {
                    val profile = result.data
                    _uiState.update { currentState ->
                        val draft = currentState.draftApplication ?: createEmptyApplication()
                        currentState.copy(
                            draftApplication = draft.copy(
                                // Pre-fill basic info if needed in the request model
                                // Note: LoanApplicationRequest might need more fields from UserProfile
                            )
                        )
                    }
                }
                else -> {}
            }
        }
    }

    /**
     * Cập nhật cấu hình khoản vay từ Step 1
     */
    fun updateLoanConfig(
        packageId: String?,
        amount: Long,
        tenor: Int,
        hasInsurance: Boolean,
        voucherId: String?
    ) {
        _uiState.update { currentState ->
            val draft = currentState.draftApplication ?: createEmptyApplication()
            currentState.copy(
                draftApplication = draft.copy(
                    packageId = packageId,
                    loanAmount = amount,
                    tenorMonth = tenor,
                    hasInsurance = hasInsurance,
                    ekycMatchKey = null,
                    voucherId = voucherId
                ),
                loanId = currentState.loanId ?: "LOAN-${System.currentTimeMillis().toString().takeLast(6)}"
            )
        }
    }

    /**
     * Cập nhật toàn bộ thông tin đơn đăng ký (thường gọi sau Step 2)
     */
    fun updateApplicationDraft(updatedDraft: LoanApplicationRequest) {
        _uiState.update { it.copy(draftApplication = updatedDraft) }
    }

    private fun createEmptyApplication() = LoanApplicationRequest(
        packageId = null,
        loanAmount = 0, tenorMonth = 0, hasInsurance = false,
        ekycMatchKey = null,
        voucherId = null,
        permanentProvince = "", permanentDistrict = "", permanentWard = "", permanentDetail = "",
        currentProvince = "", currentDistrict = "", currentWard = "", currentDetail = "",
        monthlyIncome = 0, profession = "", position = "", education = "", maritalStatus = "",
        contactName = "", contactRelationship = "", contactPhone = ""
    )

    fun continueAfterLoanConfig() {
        val draft = _uiState.value.draftApplication
        val packageId = draft?.packageId
        if (packageId.isNullOrBlank()) {
            onNextStep()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMatchingEkyc = true, ekycMatchError = null) }
            when (val result = loanRepository.matchEkyc(packageId)) {
                is Resource.Success -> {
                    val match = result.data
                    // Always show EKYC Intro if required by flow, but store the key if already matched
                    _uiState.update { state ->
                        state.copy(
                            isMatchingEkyc = false,
                            currentStep = 2,
                            subState = LoanSubState.EKYC_INTRO,
                            draftApplication = state.draftApplication?.copy(ekycMatchKey = match.ekycMatchKey)
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isMatchingEkyc = false,
                            currentStep = 2,
                            subState = LoanSubState.EKYC_INTRO,
                            ekycMatchError = result.message
                        )
                    }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun ensureEkycMatchThenSubmit(onReady: (LoanApplicationRequest?) -> Unit) {
        val draft = _uiState.value.draftApplication
        val packageId = draft?.packageId
        if (draft == null || packageId.isNullOrBlank() || !draft.ekycMatchKey.isNullOrBlank()) {
            onReady(draft)
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isMatchingEkyc = true, ekycMatchError = null) }
            when (val result = loanRepository.matchEkyc(packageId)) {
                is Resource.Success -> {
                    val updated = if (result.data.isMatched && result.data.canApplyLoan && !result.data.ekycMatchKey.isNullOrBlank()) {
                        draft.copy(ekycMatchKey = result.data.ekycMatchKey)
                    } else {
                        null
                    }
                    _uiState.update {
                        it.copy(
                            isMatchingEkyc = false,
                            ekycMatchError = if (updated == null) result.data.message else null,
                            draftApplication = updated ?: it.draftApplication
                        )
                    }
                    onReady(updated)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isMatchingEkyc = false, ekycMatchError = result.message) }
                    onReady(null)
                }
                Resource.Loading -> Unit
            }
        }
    }

    /**
     * Chuyển sang giai đoạn tiếp theo trong luồng
     */
    fun onNextStep() {
        _uiState.update { currentState ->
            val (nextStep, nextSubState) = when (currentState.subState) {
                LoanSubState.CONFIG -> 2 to LoanSubState.EKYC_INTRO
                LoanSubState.EKYC_INTRO -> 2 to LoanSubState.EKYC_CAPTURE
                LoanSubState.EKYC_DOCUMENT -> 2 to LoanSubState.EKYC_CAPTURE
                LoanSubState.EKYC_CAPTURE -> 2 to LoanSubState.CUSTOMER_FORM
                LoanSubState.CUSTOMER_FORM -> 3 to LoanSubState.CONFIRM_FORM
                LoanSubState.CONFIRM_FORM -> 3 to LoanSubState.REGISTRATION_SUCCESS
                LoanSubState.REGISTRATION_SUCCESS -> 3 to LoanSubState.REGISTRATION_SUCCESS
            }
            currentState.copy(currentStep = nextStep, subState = nextSubState)
        }
    }

    /**
     * Quay lại giai đoạn trước đó
     */
    fun onPreviousStep() {
        _uiState.update { currentState ->
            val (prevStep, prevSubState) = when (currentState.subState) {
                LoanSubState.CONFIG -> 1 to LoanSubState.CONFIG
                LoanSubState.EKYC_INTRO -> 1 to LoanSubState.CONFIG
                LoanSubState.EKYC_DOCUMENT -> 2 to LoanSubState.EKYC_INTRO
                LoanSubState.EKYC_CAPTURE -> 2 to LoanSubState.EKYC_INTRO
                LoanSubState.CUSTOMER_FORM -> 2 to LoanSubState.EKYC_INTRO
                LoanSubState.CONFIRM_FORM -> 2 to LoanSubState.CUSTOMER_FORM
                LoanSubState.REGISTRATION_SUCCESS -> 3 to LoanSubState.REGISTRATION_SUCCESS
            }
            currentState.copy(currentStep = prevStep, subState = prevSubState)
        }
    }

    fun updateSubState(subState: LoanSubState) {
        _uiState.update { it.copy(subState = subState) }
    }

    fun clearEkycMatchError() {
        _uiState.update { it.copy(ekycMatchError = null) }
    }

    fun onFaceCaptureUploaded() {
        val packageId = _uiState.value.draftApplication?.packageId
        if (packageId == null) {
            onNextStep()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isMatchingEkyc = true, ekycMatchError = null) }
            when (val matchResult = loanRepository.matchEkyc(packageId)) {
                is Resource.Success -> {
                    val match = matchResult.data
                    if (match.isMatched && match.canApplyLoan && !match.ekycMatchKey.isNullOrBlank()) {
                        _uiState.update { state ->
                            state.copy(
                                isMatchingEkyc = false,
                                draftApplication = state.draftApplication?.copy(ekycMatchKey = match.ekycMatchKey)
                            )
                        }
                        onNextStep()
                    } else {
                        _uiState.update {
                            it.copy(
                                isMatchingEkyc = false,
                                ekycMatchError = match.message
                            )
                        }
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isMatchingEkyc = false, ekycMatchError = matchResult.message) }
                }
                Resource.Loading -> Unit
            }
        }
    }

    fun onFaceCaptureError(message: String) {
        _uiState.update { it.copy(isMatchingEkyc = false, ekycMatchError = message) }
    }

    fun toggleExitDialog(show: Boolean) {
        _uiState.update { it.copy(showExitDialog = show) }
    }

    fun setStep(step: Int) {
        _uiState.update { currentState ->
            val nextSubState = when (step) {
                1 -> LoanSubState.CONFIG
                2 -> LoanSubState.EKYC_INTRO
                3 -> LoanSubState.CONFIRM_FORM
                else -> currentState.subState
            }
            currentState.copy(currentStep = step, subState = nextSubState)
        }
    }
}
