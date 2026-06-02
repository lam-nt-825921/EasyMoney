package com.example.easymoney.domain.model

/**
 * Workflow #71 — estimate for a debt repayment (monthly or full early settlement),
 * fetched before the user confirms. Money fields are kept as [Double] to match the
 * backend JSON-number contract; round only at display time.
 */
data class RepaymentEstimate(
    val debtId: Long,
    val repayType: RepayType,
    val paymentMethod: String?,
    val amountDue: Double,
    val principalDue: Double,
    val interestDue: Double,
    val penaltyFee: Double,
    val discountAmount: Double,
    val rewardPointsPreview: Int?,
    val currency: String,
    val debtStatusAfterPayment: String?
)
