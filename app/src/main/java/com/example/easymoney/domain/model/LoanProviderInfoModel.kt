package com.example.easymoney.domain.model

data class LoanProviderInfoModel(
    val organizationName: String,
    val hotline: String,
    val address: String,
    val supportUrl: String? = null
)

