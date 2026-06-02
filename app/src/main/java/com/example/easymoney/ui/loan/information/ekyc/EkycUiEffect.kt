package com.example.easymoney.ui.loan.information.ekyc

import com.example.easymoney.utils.UiText

sealed class EkycUiEffect {
    data object NavigateToNextStep : EkycUiEffect()
    data object NavigateBack : EkycUiEffect()
    data object OpenSettings : EkycUiEffect()
    data class ShowToast(val message: UiText) : EkycUiEffect()
    // Workflow #68 — error effect now carries UiText so screen renders localised copy.
    data class ShowError(val message: UiText) : EkycUiEffect()
}
