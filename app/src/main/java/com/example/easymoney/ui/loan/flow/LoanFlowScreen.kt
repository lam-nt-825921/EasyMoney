package com.example.easymoney.ui.loan.flow

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.easymoney.R
import com.example.easymoney.navigation.AppDestination
import com.example.easymoney.ui.components.AppTopBarOverride
import com.example.easymoney.ui.components.RegisterTopBarOverride
import com.example.easymoney.ui.loan.LoanViewModel
import com.example.easymoney.ui.loan.components.LoanStepper
import com.example.easymoney.ui.loan.configuration.LoanConfigurationScreen
import com.example.easymoney.ui.loan.information.ekyc.EkycIntroScreen

@Composable
fun LoanFlowScreen(
    viewModel: LoanViewModel,
    onExitFlow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep = uiState.currentStep
    val topBarTitle = when (currentStep) {
        1 -> "Thông tin khoản vay"
        2 -> "eKYC"
        else -> "Thông tin khoản vay"
    }
    val onFlowBack: () -> Unit = {
        if (currentStep > 1) viewModel.onPreviousStep() else onExitFlow()
    }

    RegisterTopBarOverride(
        ownerRoute = AppDestination.LoanFlow.route,
        override = AppTopBarOverride(
            title = topBarTitle,
            showBackButton = true,
            showHelpButton = false,
            onBackClick = onFlowBack
        )
    )

    BackHandler(enabled = currentStep > 1) {
        viewModel.onPreviousStep()
    }

    Column(modifier = modifier.fillMaxSize()) {

        LoanStepper(
            currentStep = currentStep,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (currentStep) {
            1 -> LoanConfigurationScreen(
                viewModel = viewModel,
                onBackClick = onExitFlow,
                modifier = Modifier.fillMaxSize()
            )

            2 -> EkycIntroScreen(
                illustrationResId = R.drawable.ekyc_preview_img,
                onStartCaptureClick = { /* TODO: connect capture flow */ },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}



