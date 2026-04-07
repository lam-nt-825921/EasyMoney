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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.easymoney.R
import com.example.easymoney.navigation.AppDestination
import com.example.easymoney.ui.components.AppTopBarOverride
import com.example.easymoney.ui.components.RegisterTopBarOverride
import com.example.easymoney.ui.loan.components.LoanExitDialog
import com.example.easymoney.ui.loan.components.LoanStepper
import com.example.easymoney.ui.loan.configuration.LoanConfigurationScreen
import com.example.easymoney.ui.loan.information.confirm.ConfirmLoanInformationScreen
import com.example.easymoney.ui.loan.information.ekyc.EkycCameraViewModel
import com.example.easymoney.ui.loan.information.ekyc.EkycFaceCaptureScreen
import com.example.easymoney.ui.loan.information.ekyc.EkycIntroScreen
import com.example.easymoney.ui.loan.information.form.LoanInformationFormScreen
import com.example.easymoney.ui.loan.information.form.LoanInformationFormViewModel

@Composable
fun LoanFlowScreen(
    onCancel: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanFlowViewModel = hiltViewModel(),
    ekycViewModel: EkycCameraViewModel = hiltViewModel(),
    formViewModel: LoanInformationFormViewModel = hiltViewModel() 
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep = uiState.currentStep
    val subState = uiState.subState
    
    val formUiState by formViewModel.uiState.collectAsState()

    val topBarTitle = when (currentStep) {
        1 -> "Thông tin khoản vay"
        2 -> if (subState == LoanSubState.CUSTOMER_FORM) "Thông tin cá nhân" else "Xác thực khuôn mặt"
        3 -> "Xác nhận thông tin"
        else -> "Thông tin khoản vay"
    }

    LaunchedEffect(subState) {
        if (subState == LoanSubState.CONFIG || subState == LoanSubState.EKYC_INTRO) {
            ekycViewModel.resetState()
        }
    }

    // Logic xử lý khi nhấn Back
    val handleBack = {
        if (currentStep == 1) {
            onCancel() // Ở step 1 thì thoát thẳng
        } else {
            viewModel.toggleExitDialog(true)
        }
    }

    RegisterTopBarOverride(
        ownerRoute = AppDestination.LoanFlow.route,
        override = AppTopBarOverride(
            title = topBarTitle,
            showBackButton = true,
            showHelpButton = false,
            onBackClick = handleBack
        )
    )

    BackHandler(enabled = true) {
        handleBack()
    }

    if (uiState.showExitDialog) {
        LoanExitDialog(
            onDismiss = { viewModel.toggleExitDialog(false) },
            onConfirm = { 
                viewModel.toggleExitDialog(false)
                onCancel() // Hủy đăng ký -> Về Onboarding
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (subState != LoanSubState.EKYC_CAPTURE) {
            LoanStepper(
                currentStep = currentStep,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        when (currentStep) {
            1 -> LoanConfigurationScreen(
                onNextStep = viewModel::onNextStep,
                onBackClick = onCancel,
                modifier = Modifier.fillMaxSize()
            )

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
                        EkycFaceCaptureScreen(
                            viewModel = ekycViewModel,
                            onBackToIntro = { viewModel.updateSubState(LoanSubState.EKYC_INTRO) },
                            onSuccess = { viewModel.onNextStep() },
                            onNavigateToError = { },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LoanSubState.CUSTOMER_FORM -> {
                        LoanInformationFormScreen(
                            viewModel = formViewModel,
                            onNextStep = { viewModel.onNextStep() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {}
                }
            }

            3 -> {
                ConfirmLoanInformationScreen(
                    formData = formUiState,
                    onConfirmed = { 
                        onComplete() // Xác nhận thành công
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
