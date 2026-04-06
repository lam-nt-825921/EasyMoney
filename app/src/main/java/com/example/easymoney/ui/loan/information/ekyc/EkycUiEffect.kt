package com.example.easymoney.ui.loan.information.ekyc

sealed class EkycUiEffect {
    data object NavigateToNextStep : EkycUiEffect()
    data object NavigateBack : EkycUiEffect()
    data object OpenSettings : EkycUiEffect()
    data class ShowToast(val message: String) : EkycUiEffect()
    data class ShowError(val message: String) : EkycUiEffect()
}

