package com.example.easymoney.domain.model

data class LoanPackageModel(
    val id: String,
    val packageName: String,
    val tenorRange: String,
    val minAmount: Long,
    val maxAmount: Long,
    val interest: Double,
    val overdueCost: Double,
    val eligibleCreditScore: Int,
    val isEligible: Boolean = true,
    val ineligibilityReason: String? = null,
    // Workflow #29 — filter metadata
    val isHot: Boolean = false,
    val isNew: Boolean = false,
    val isPromotional: Boolean = false,
    val badges: List<String> = emptyList()
) {
    fun getTenorList(defaultTenors: List<Int> = listOf(6, 12, 18, 24)): List<Int> {
        return try {
            tenorRange.split(",").map { it.trim().toInt() }
        } catch (_: Exception) {
            defaultTenors
        }
    }
}

