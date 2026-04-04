package com.example.easymoney.ui.loan.flow

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.easymoney.domain.repository.LoanRepositoryImpl
import com.example.easymoney.ui.loan.LoanViewModel
import com.example.easymoney.ui.theme.EasyMoneyTheme

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoanFlowPreview() {
    val viewModel = remember {
        LoanViewModel(LoanRepositoryImpl()).apply {
            loadLoanPackage()
        }
    }

    EasyMoneyTheme {
        LoanFlowScreen(
            viewModel = viewModel,
            onExitFlow = {}
        )
    }
}

