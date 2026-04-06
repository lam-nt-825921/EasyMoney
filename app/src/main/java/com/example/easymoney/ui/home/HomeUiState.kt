package com.example.easymoney.ui.home

data class HomeUiState(
    val userName: String = "",
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

