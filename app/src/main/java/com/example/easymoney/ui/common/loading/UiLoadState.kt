package com.example.easymoney.ui.common.loading

sealed interface UiLoadState {
    data object Idle : UiLoadState
    data object InitialLoading : UiLoadState
    data object Refreshing : UiLoadState
    data object Submitting : UiLoadState
    data class Error(val message: String? = null) : UiLoadState
}

