package com.example.easymoney.ui.loan.flow

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LoanFlowViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(LoanFlowModel())
    val uiState: StateFlow<LoanFlowModel> = _uiState.asStateFlow()

    /**
     * Chuyển sang giai đoạn tiếp theo trong luồng
     */
    fun onNextStep() {
        _uiState.update { currentState ->
            val (nextStep, nextSubState) = when (currentState.subState) {
                LoanSubState.CONFIG -> 2 to LoanSubState.EKYC_INTRO
                LoanSubState.EKYC_INTRO -> 2 to LoanSubState.EKYC_CAPTURE
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
