package com.example.easymoney.domain.model

data class LoanProduct(
    val id: String,
    val name: String,
    val interestRate: Double,
    val maxAmount: Long,
    val isHot: Boolean = false,
    val badge: String? = null,
    val description: String? = null
)
