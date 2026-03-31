package com.example.easymoney.ui.loan.configuration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.easymoney.ui.loan.LoanViewModel

/**
 * Màn hình cấu hình khoản vay.
 * Kết nối [LoanViewModel] với giao diện [LoanConfigurationContent].
 */
@Composable
fun LoanConfigurationScreen(
    viewModel: LoanViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LoanConfigurationContent(
        uiState = uiState,
        onAmountChanged = viewModel::onAmountChanged,
        onTenorSelected = viewModel::onTenorSelected,
        onInsuranceToggled = viewModel::onInsuranceToggled,
        onNextStep = viewModel::onNextStep,
        modifier = modifier
    )
}
