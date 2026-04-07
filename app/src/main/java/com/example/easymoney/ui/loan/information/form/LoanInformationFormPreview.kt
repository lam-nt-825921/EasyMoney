package com.example.easymoney.ui.loan.information.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.easymoney.domain.repository.LoanRepositoryImpl
import com.example.easymoney.ui.theme.EasyMoneyTheme

/**
 * Preview cho màn hình điền thông tin vay vốn (Step 3).
 * Sử dụng repository mẫu để hiển thị dữ liệu địa chỉ thường trú.
 */
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoanInformationFormPreview() {
    val viewModel = remember {
        LoanInformationFormViewModel(LoanRepositoryImpl())
    }

    EasyMoneyTheme {
        LoanInformationFormScreen(
            onNextStep = { /* Mock next step */ },
            viewModel = viewModel
        )
    }
}
