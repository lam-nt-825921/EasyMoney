package com.example.easymoney.ui.loan.configuration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.easymoney.domain.repository.LoanRepositoryImpl
import com.example.easymoney.ui.loan.LoanViewModel
import com.example.easymoney.ui.theme.EasyMoneyTheme

/**
 * HƯỚNG DẪN SỬ DỤNG MÀN HÌNH CẤU HÌNH KHOẢN VAY
 *
 * 1. Để sử dụng trong code thật (Activity/Fragment/NavHost):
 *    Sử dụng [LoanConfigurationScreen]. Bạn cần cung cấp một instance của [LoanViewModel].
 *
 *    Ví dụ:
 *    val viewModel: LoanViewModel = viewModel(factory = ...)
 *    LoanConfigurationScreen(viewModel = viewModel, onBackClick = { })
 *
 * 2. Logic tính toán:
 *    Màn hình tự động cập nhật các con số (tiền nhận, tiền trả hàng tháng...) thông qua
 *    ViewModel mỗi khi người dùng thay đổi số tiền hoặc kỳ hạn.
 *
 * 3. Dữ liệu đầu vào:
 *    Cần gọi `viewModel.loadLoanPackage(id)` để khởi tạo dữ liệu cho màn hình.
 */

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoanConfigurationPreview() {
    val viewModel = remember {
        LoanViewModel(LoanRepositoryImpl()).apply {
            loadLoanPackage()
        }
    }

    EasyMoneyTheme {
        LoanConfigurationScreen(
            viewModel = viewModel,
            onBackClick = { /* Không xử lý trong preview */ }
        )
    }
}
