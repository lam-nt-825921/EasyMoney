package com.example.easymoney.ui.confirmation

import com.example.easymoney.domain.model.MyInfoModel

data class ConfirmInfoUiState(
    val sectionTitle: String = "Thông tin cá nhân",
    val continueButtonText: String = "Tiếp tục",
    val editInfoText: String = "Tôi muốn sửa thông tin",
    val userInfo: MyInfoModel? = null,
    val loadState: ConfirmInfoLoadState = ConfirmInfoLoadState.InitialLoading
)

sealed class ConfirmInfoLoadState {
    data object InitialLoading : ConfirmInfoLoadState()
    data object Loading : ConfirmInfoLoadState()
    data object Success : ConfirmInfoLoadState()
    data class Error(val message: String) : ConfirmInfoLoadState()
}
