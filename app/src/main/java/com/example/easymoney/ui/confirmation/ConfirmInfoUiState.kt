package com.example.easymoney.ui.confirmation

import com.example.easymoney.domain.model.MyInfoModel

data class ConfirmInfoUiState(
    val sectionTitle: String = "Thong tin ca nhan",
    val continueButtonText: String = "Tiep tuc",
    val editInfoText: String = "Toi muon sua thong tin",
    val userInfo: MyInfoModel? = null,
    val loadState: ConfirmInfoLoadState = ConfirmInfoLoadState.InitialLoading
)

sealed class ConfirmInfoLoadState {
    data object InitialLoading : ConfirmInfoLoadState()
    data object Loading : ConfirmInfoLoadState()
    data object Success : ConfirmInfoLoadState()
    data class Error(val message: String) : ConfirmInfoLoadState()
}


