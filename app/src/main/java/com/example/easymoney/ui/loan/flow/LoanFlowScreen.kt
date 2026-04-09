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
import com.example.easymoney.ui.components.TopBarMode
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
    viewModel: LoanFlowViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep = uiState.currentStep
    val subState = uiState.subState
    
    // 1. Quản lý TopBar (Ẩn khi thành công)
    val isSuccessScreen = subState == LoanSubState.REGISTRATION_SUCCESS
    
    val topBarTitle = when (currentStep) {
        1 -> "Thông tin khoản vay"
        2 -> if (subState == LoanSubState.CUSTOMER_FORM) "Thông tin cá nhân" else "Xác thực khuôn mặt"
        3 -> if (isSuccessScreen) "" else "Xác nhận thông tin"
        else -> "Thông tin khoản vay"
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
                    onCancel()
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
                onCancel()
            } else {
                viewModel.toggleExitDialog(true)
            }
        }
        // Nếu isSuccessScreen = true, nút Back vật lý sẽ bị "nuốt" - không làm gì cả
    }

    if (uiState.showExitDialog) {
        LoanExitDialog(
            onDismiss = { viewModel.toggleExitDialog(false) },
            onConfirm = { 
                viewModel.toggleExitDialog(false)
                onCancel()
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
                            onNextStep = { viewModel.onNextStep() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {}
                }
            }

            3 -> {
                when (subState) {
                    LoanSubState.CONFIRM_FORM -> {
                        val formViewModel: LoanInformationFormViewModel = hiltViewModel()
                        val formUiState by formViewModel.uiState.collectAsState()
                        ConfirmLoanInformationScreen(
                            formData = formUiState,
                            onConfirmed = {
                                viewModel.onNextStep() // Chuyển sang thành công
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
