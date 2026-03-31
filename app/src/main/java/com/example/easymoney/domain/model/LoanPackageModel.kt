package com.example.easymoney.domain.model

data class LoanPackageModel(
    val id: String,
    val packageName: String,
    val tenorRange: String,
    val minAmount: Long,
    val maxAmount: Long,
    val interest: Double,
    val overdueCost: Double,
    val eligibleCreditScore: Int
) {
    fun getTenorList(defaultTenors: List<Int> = listOf(6, 12, 18, 24)): List<Int> {
        return try {
            tenorRange.split(",").map { it.trim().toInt() }
        } catch (_: Exception) {
            defaultTenors
        }
    }
}

