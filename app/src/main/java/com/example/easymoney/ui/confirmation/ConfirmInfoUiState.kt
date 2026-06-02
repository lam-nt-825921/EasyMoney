package com.example.easymoney.ui.confirmation

import com.example.easymoney.domain.model.MyInfoModel
import com.example.easymoney.utils.UiText

data class ConfirmInfoUiState(
    val userInfo: MyInfoModel? = null,
    val loadState: ConfirmInfoLoadState = ConfirmInfoLoadState.InitialLoading
)

sealed class ConfirmInfoLoadState {
    data object InitialLoading : ConfirmInfoLoadState()
    data object Loading : ConfirmInfoLoadState()
    data object Success : ConfirmInfoLoadState()
    // Workflow #65 — error message now uses UiText so screen renders localised copy.
    data class Error(val message: UiText) : ConfirmInfoLoadState()
}
