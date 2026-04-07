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
import com.example.easymoney.ui.loan.information.confirm.ConfirmLoanInformationScreen
import com.example.easymoney.ui.loan.information.ekyc.EkycCameraViewModel
import com.example.easymoney.ui.loan.information.ekyc.EkycFaceCaptureScreen
import com.example.easymoney.ui.loan.information.ekyc.EkycIntroScreen
import com.example.easymoney.ui.loan.information.form.LoanInformationFormScreen
import com.example.easymoney.ui.loan.information.form.LoanInformationFormViewModel

@Composable
fun LoanFlowScreen(
    onExitFlow: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoanFlowViewModel = hiltViewModel(),
    ekycViewModel: EkycCameraViewModel = hiltViewModel(),
    // Lấy FormViewModel ở đây để dữ liệu được giữ lại xuyên suốt Flow (Singleton-like in this scope)
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
                        // Khi xác nhận thành công, tiến tới bước phê duyệt (chưa code)
                        onExitFlow() 
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
