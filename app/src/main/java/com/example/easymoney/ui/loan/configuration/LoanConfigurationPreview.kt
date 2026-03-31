package com.example.easymoney.ui.loan.configuration

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.easymoney.data.UserPreferencesRepository
import com.example.easymoney.data.model.LoanPackage
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
 *    Cần gọi `viewModel.setLoanPackage(package)` để khởi tạo dữ liệu cho màn hình.
 */

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoanConfigurationPreview() {
    val context = LocalContext.current
    
    // 1. Tạo dữ liệu mẫu
    val mockPackage = LoanPackage(
        id = "1",
        packageName = "Vay Nhanh 24/7",
        tenorRange = "6,12,18,24",
        minAmount = 5_000_000,
        maxAmount = 50_000_000,
        interest = 12.0,
        overdueCost = 5.0,
        eligibleCreditScore = 600
    )

    // 2. Khởi tạo ViewModel trực tiếp trong Preview (Sandbox mode)
    val viewModel = remember {
        LoanViewModel(UserPreferencesRepository(context)).apply {
            setLoanPackage(mockPackage)
        }
    }

    EasyMoneyTheme {
        // 3. Gọi màn hình chính thay vì gọi trực tiếp Content component
        LoanConfigurationScreen(
            viewModel = viewModel,
            onBackClick = { /* Không xử lý trong preview */ }
        )
    }
}
