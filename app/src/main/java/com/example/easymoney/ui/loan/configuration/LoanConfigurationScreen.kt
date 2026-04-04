package com.example.easymoney.ui.loan.configuration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.easymoney.ui.loan.LoanPackageLoadState
import com.example.easymoney.ui.loan.LoanViewModel

/**
 * Màn hình cấu hình khoản vay.
 * Kết nối [LoanViewModel] với giao diện [LoanConfigurationContent].
 */
@Composable
fun LoanConfigurationScreen(
    viewModel: LoanViewModel,
    @Suppress("UNUSED_PARAMETER")
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val shouldShowSkeleton = uiState.packageLoadState is LoanPackageLoadState.Loading ||
        (uiState.isLoading && uiState.selectedPackage == null)

    if (shouldShowSkeleton) {
        LoanConfigurationSkeleton(modifier = modifier)
        return
    }

    LoanConfigurationContent(
        uiState = uiState,
        onAmountChanged = viewModel::onAmountChanged,
        onTenorSelected = viewModel::onTenorSelected,
        onInsuranceToggled = viewModel::onInsuranceToggled,
        onNextStep = viewModel::onNextStep,
        modifier = modifier
    )
}
