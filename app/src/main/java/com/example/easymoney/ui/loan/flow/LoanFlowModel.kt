package com.example.easymoney.ui.loan.flow

import com.example.easymoney.domain.model.LoanApplicationRequest


/**
 * Các giai đoạn chi tiết trong luồng vay
 */
enum class LoanSubState {
    CONFIG,             // Step 1: Cấu hình khoản vay
    EKYC_INTRO,         // Step 2: Hướng dẫn eKYC
    EKYC_CAPTURE,       // Step 2: Chụp ảnh eKYC
    CUSTOMER_FORM,      // Step 2: Điền form thông tin
    CONFIRM_FORM,       // Step 3: Xác nhận thông tin & Ký hợp đồng
    REGISTRATION_SUCCESS // Màn hình thông báo gửi thành công
}

/**
 * Trạng thái hồ sơ rút gọn
 */
enum class LoanStatus {
    DRAFT,              // Đang điền hồ sơ
    SUBMITTED,          // Đã gửi hồ sơ
    APPROVED,           // Đã duyệt
    REJECTED            // Đã từ chối
}

data class LoanFlowModel(
    val currentStep: Int = 1,
    val subState: LoanSubState = LoanSubState.CONFIG,
    val status: LoanStatus = LoanStatus.DRAFT,
    val showExitDialog: Boolean = false,

    // Dữ liệu dùng chung giữa các bước
    val loanId: String? = null,
    val draftApplication: LoanApplicationRequest? = null
)
