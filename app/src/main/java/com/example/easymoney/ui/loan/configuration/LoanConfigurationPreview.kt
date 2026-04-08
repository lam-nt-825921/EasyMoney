package com.example.easymoney.ui.loan.configuration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.easymoney.domain.repository.LoanRepositoryImpl
import com.example.easymoney.ui.theme.EasyMoneyTheme

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoanConfigurationPreview() {
    val viewModel = remember {
        LoanConfigurationViewModel(LoanRepositoryImpl()).apply {
            loadLoanPackage()
        }
    }

    EasyMoneyTheme {
        LoanConfigurationScreen(
            onNextStep = {},
            onBackClick = { },
            viewModel = viewModel
        )
    }
}
