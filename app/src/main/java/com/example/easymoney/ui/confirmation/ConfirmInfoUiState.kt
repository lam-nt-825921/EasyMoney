package com.example.easymoney.ui.confirmation

data class PersonalInfoItem(
    val label: String,
    val value: String
)

data class ConfirmInfoUiState(
    val sectionTitle: String,
    val sourceHint: String,
    val personalInfoItems: List<PersonalInfoItem>,
    val continueButtonText: String,
    val editInfoText: String
) {
    companion object {
        // Temporary mock data for UI wiring. Replace this with repository data later.
        fun mock(): ConfirmInfoUiState = ConfirmInfoUiState(
            sectionTitle = "Thông tin cá nhân",
            sourceHint = "Nguồn dữ liệu hiện tại: mock data (sẽ thay bằng repository).",
            personalInfoItems = listOf(
                PersonalInfoItem(label = "Họ và tên", value = "Nguyễn Đức Minh"),
                PersonalInfoItem(label = "Giới tính", value = "Nam"),
                PersonalInfoItem(label = "Ngày sinh", value = "01/05/2005"),
                PersonalInfoItem(label = "Số điện thoại", value = "0936-552-900"),
                PersonalInfoItem(label = "CMND/CCCD", value = "093201403413"),
                PersonalInfoItem(label = "Ngày cấp", value = "03/11/2020")
            ),
            continueButtonText = "Tiếp tục",
            editInfoText = "Tôi muốn sửa thông tin"
        )
    }
}


