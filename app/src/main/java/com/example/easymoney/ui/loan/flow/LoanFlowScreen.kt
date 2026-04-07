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
import com.example.easymoney.ui.loan.components.LoanStepper
import com.example.easymoney.ui.loan.configuration.LoanConfigurationScreen
import com.example.easymoney.ui.loan.information.ekyc.EkycCameraViewModel
import com.example.easymoney.ui.loan.information.ekyc.EkycFaceCaptureScreen
import com.example.easymoney.ui.loan.information.ekyc.EkycIntroScreen
import com.example.easymoney.ui.loan.information.esign.LoanEsignScreen
import com.example.easymoney.ui.loan.information.form.LoanInformationFormScreen

@Composable
fun LoanFlowScreen(
    onExitFlow: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanFlowViewModel = hiltViewModel(),
    ekycViewModel: EkycCameraViewModel = hiltViewModel() 
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep = uiState.currentStep
    val subState = uiState.subState

    val topBarTitle = when (currentStep) {
        1 -> "Thông tin khoản vay"
        2 -> if (subState == LoanSubState.CUSTOMER_FORM) "Thông tin cá nhân" else "Xác thực khuôn mặt"
        3 -> "Ký hợp đồng"
        else -> "Thông tin khoản vay"
    }

    // Reset EKYC state khi quay lại step 1 hoặc vào lại Intro của Step 2
    LaunchedEffect(subState) {
        if (subState == LoanSubState.CONFIG || subState == LoanSubState.EKYC_INTRO) {
            ekycViewModel.resetState()
        }
    }

    RegisterTopBarOverride(
        ownerRoute = AppDestination.LoanFlow.route,
        override = AppTopBarOverride(
            title = topBarTitle,
            showBackButton = true,
            showHelpButton = false,
            onBackClick = {
                if (currentStep == 1) onExitFlow() else viewModel.onPreviousStep()
            }
        )
    )

    BackHandler(enabled = currentStep > 1) {
        viewModel.onPreviousStep()
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Chỉ hiển thị stepper nếu không phải đang trong màn capture
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
                onBackClick = onExitFlow,
                modifier = Modifier.fillMaxSize()
            )

            2 -> {
                when (subState) {
                    LoanSubState.EKYC_INTRO -> {
                        EkycIntroScreen(
                            illustrationResId = R.drawable.ekyc_preview_img,
                            onStartCaptureClick = { 
                                viewModel.updateSubState(LoanSubState.EKYC_CAPTURE) 
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LoanSubState.EKYC_CAPTURE -> {
                        EkycFaceCaptureScreen(
                            viewModel = ekycViewModel,
                            onBackToIntro = { 
                                viewModel.updateSubState(LoanSubState.EKYC_INTRO) 
                            },
                            onSuccess = { 
                                viewModel.onNextStep() // Chuyển sang Form
                            },
                            onNavigateToError = { error ->
                                // Screen tự hiển thị lỗi, ta chỉ cần chờ người dùng bấm retake trong screen
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    LoanSubState.CUSTOMER_FORM -> {
                        LoanInformationFormScreen(
                            onNextStep = { 
                                viewModel.onNextStep() // Chuyển sang Esign
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {}
                }
            }

            3 -> {
                LoanEsignScreen(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
