package com.example.easymoney.ui.loan.flow

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.togetherWith
import com.example.easymoney.R
import com.example.easymoney.ui.components.AppNavigationBar
import com.example.easymoney.ui.loan.LoanViewModel
import com.example.easymoney.ui.loan.components.LoanStepper
import com.example.easymoney.ui.loan.configuration.LoanConfigurationScreen
import com.example.easymoney.ui.loan.information.ekyc.EkycIntroScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LoanFlowScreen(
    viewModel: LoanViewModel,
    onExitFlow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentStep = uiState.currentStep

    BackHandler(enabled = currentStep > 1) {
        viewModel.onPreviousStep()
    }

    Column(modifier = modifier.fillMaxSize()) {
        AppNavigationBar(
            title = "Thông tin khoản vay",
            showBackButton = true,
            showHelpButton = false,
            onBackClick = {
                if (currentStep > 1) viewModel.onPreviousStep() else onExitFlow()
            },
            backgroundColor = MaterialTheme.colorScheme.background,
            contentColor = Color.Unspecified
        )

        LoanStepper(
            currentStep = currentStep,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        AnimatedContent(
            targetState = currentStep,
            label = "loan_flow_step",
            transitionSpec = {
                val forward = targetState > initialState
                (
                    slideInHorizontally(
                        animationSpec = tween(250),
                        initialOffsetX = { fullWidth -> if (forward) fullWidth else -fullWidth }
                    ) + fadeIn(animationSpec = tween(250))
                ) togetherWith (
                    slideOutHorizontally(
                        animationSpec = tween(250),
                        targetOffsetX = { fullWidth -> if (forward) -fullWidth else fullWidth }
                    ) + fadeOut(animationSpec = tween(250))
                )
            }
        ) { step ->
            when (step) {
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
}



