package com.example.easymoney.ui.loan.flow

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
import com.example.easymoney.navigation.AppDestination
import com.example.easymoney.ui.components.AppTopBarOverride
import com.example.easymoney.ui.components.RegisterTopBarOverride
import com.example.easymoney.ui.components.TopBarMode
import com.example.easymoney.ui.loan.components.LoanExitDialog
import com.example.easymoney.ui.loan.components.LoanStepper
import com.example.easymoney.ui.loan.configuration.LoanConfigurationScreen
import com.example.easymoney.ui.loan.configuration.LoanConfigurationViewModel
import com.example.easymoney.ui.loan.information.confirm.ConfirmLoanInformationScreen
import com.example.easymoney.ui.loan.information.ekyc.EkycCameraViewModel
import com.example.easymoney.ui.loan.information.ekyc.EkycFaceCaptureScreen
import com.example.easymoney.ui.loan.information.ekyc.EkycIntroScreen
import com.example.easymoney.ui.loan.information.form.LoanInformationFormScreen
import com.example.easymoney.ui.loan.information.form.LoanInformationFormViewModel

@Composable
fun LoanFlowScreen(
    onBack: () -> Unit,
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanFlowViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep = uiState.currentStep
    val subState = uiState.subState
    
    // 1. Quản lý TopBar (Ẩn khi thành công)
    val isSuccessScreen = subState == LoanSubState.REGISTRATION_SUCCESS
    
    val topBarTitle = when (currentStep) {
        1 -> stringResource(id = R.string.loan_flow_step_1)
        2 -> if (subState == LoanSubState.CUSTOMER_FORM) stringResource(id = R.string.loan_flow_step_2_personal) else stringResource(id = R.string.loan_flow_step_2_ekyc)
        3 -> if (isSuccessScreen) "" else stringResource(id = R.string.loan_flow_step_3)
        else -> stringResource(id = R.string.loan_flow_step_1)
    }

    RegisterTopBarOverride(
        ownerRoute = AppDestination.LoanFlow.route,
        override = AppTopBarOverride(
            topBarMode = if (isSuccessScreen) TopBarMode.HIDDEN else TopBarMode.STANDARD,
            title = topBarTitle,
            showBackButton = !isSuccessScreen,
            showHelpButton = false,
            onBackClick = {
                if (currentStep == 1) {
                    onBack() // Quay lại màn hình trước đó (Xác nhận thông tin)
                } else {
                    viewModel.toggleExitDialog(true)
                }
            }
        )
    )

    // 2. Chặn nút Back vật lý khi ở màn hình thành công
    BackHandler(enabled = true) {
        if (!isSuccessScreen) {
            if (currentStep == 1) {
                onBack() // Quay lại màn hình trước đó
            } else {
                viewModel.toggleExitDialog(true)
            }
        }
    }

    if (uiState.showExitDialog) {
        LoanExitDialog(
            onDismiss = { viewModel.toggleExitDialog(false) },
            onConfirm = { 
                viewModel.toggleExitDialog(false)
                onCancel() // Quay về Onboarding
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Ẩn Stepper khi ekyc capture HOẶC khi đã gửi thành công
        if (subState != LoanSubState.EKYC_CAPTURE && !isSuccessScreen) {
            LoanStepper(
                currentStep = currentStep,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (currentStep) {
            1 -> {
                val configViewModel: LoanConfigurationViewModel = hiltViewModel()
                LoanConfigurationScreen(
                    viewModel = configViewModel,
                    onNextStep = {
                        // Hoist Step 1 data to Parent
                        val state = configViewModel.uiState.value
                        viewModel.updateLoanConfig(state.loanAmount, state.selectedTenorMonth, state.isInsuranceSelected)
                        viewModel.onNextStep()
                    },
                    onBackClick = onBack,
                    modifier = Modifier.fillMaxSize()
                )
            }

            2 -> {
                when (subState) {
                    LoanSubState.EKYC_INTRO -> {
                        EkycIntroScreen(
                            illustrationResId = R.drawable.ekyc_preview_img,
                            onStartCaptureClick = { viewModel.updateSubState(LoanSubState.EKYC_CAPTURE) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LoanSubState.EKYC_CAPTURE -> {
                        val ekycViewModel: EkycCameraViewModel = hiltViewModel()
                        EkycFaceCaptureScreen(
                            viewModel = ekycViewModel,
                            onBackToIntro = { viewModel.updateSubState(LoanSubState.EKYC_INTRO) },
                            onSuccess = { viewModel.onNextStep() },
                            onNavigateToError = { },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LoanSubState.CUSTOMER_FORM -> {
                        val formViewModel: LoanInformationFormViewModel = hiltViewModel()
                        LoanInformationFormScreen(
                            viewModel = formViewModel,
                            onNextStep = {
                                // Hoist Step 2 data to Parent
                                val currentDraft = viewModel.uiState.value.draftApplication
                                if (currentDraft != null) {
                                    val updatedDraft = mapFormToRequest(currentDraft, formViewModel.uiState.value)
                                    viewModel.updateApplicationDraft(updatedDraft)
                                }
                                viewModel.onNextStep()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {}
                }
            }

            3 -> {
                when (subState) {
                    LoanSubState.CONFIRM_FORM -> {
                        // Pass aggregated data from Parent ViewModel to Step 3
                        ConfirmLoanInformationScreen(
                            loanData = uiState.draftApplication,
                            onConfirmed = {
                                viewModel.onNextStep()
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LoanSubState.REGISTRATION_SUCCESS -> {
                        LoanRegistrationSuccessScreen(
                            onBackToHome = onComplete, // Thoát khỏi luồng, reset data
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

/**
 * Helper to map Form UI State to LoanApplicationRequest object
 */
private fun mapFormToRequest(
    base: com.example.easymoney.domain.model.LoanApplicationRequest,
    form: com.example.easymoney.ui.loan.information.form.LoanInformationFormUiState
): com.example.easymoney.domain.model.LoanApplicationRequest {
    return base.copy(
        permanentProvince = form.permanentProvince?.name ?: "",
        permanentDistrict = form.permanentDistrict?.name ?: "",
        permanentWard = form.permanentWard?.name ?: "",
        permanentDetail = form.permanentDetail,
        
        currentProvince = if (form.isCurrentSameAsPermanent) form.permanentProvince?.name ?: "" else form.currentProvince?.name ?: "",
        currentDistrict = if (form.isCurrentSameAsPermanent) form.permanentDistrict?.name ?: "" else form.currentDistrict?.name ?: "",
        currentWard = if (form.isCurrentSameAsPermanent) form.permanentWard?.name ?: "" else form.currentWard?.name ?: "",
        currentDetail = if (form.isCurrentSameAsPermanent) form.permanentDetail else form.currentDetail,
        
        monthlyIncome = form.monthlyIncome.toLongOrNull() ?: 0L,
        profession = form.profession?.name ?: "",
        position = form.position?.name ?: "",
        education = form.education?.name ?: "",
        maritalStatus = form.maritalStatus?.name ?: "",
        
        contactName = form.contactName,
        contactRelationship = form.contactRelationship?.name ?: "",
        contactPhone = form.contactPhone
    )
}
