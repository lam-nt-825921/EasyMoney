package com.example.easymoney.data.model

data class OnboardingCard(
    val id: String,
    val title: String,
    val description: String,
    val icon: String? = null
)

data class ProductInfo(
    val minAmount: String,
    val maxAmount: String,
    val interestRate: String,
    val monthlyRate: String
)

data class ProviderInfo(
    val title: String,
    val description: String,
    val details: List<String>
)

