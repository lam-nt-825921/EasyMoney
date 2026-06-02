package com.example.easymoney.data.remote.dto

import com.example.easymoney.domain.model.RepayType
import com.example.easymoney.domain.model.RepaymentEstimate
import com.google.gson.annotations.SerializedName

/**
 * Workflow #71 — DTO for `GET /api/v1/loan/debts/{debt_id}/repayment-estimate`.
 * Gson is configured with LOWER_CASE_WITH_UNDERSCORES, but @SerializedName is kept
 * explicit because this DTO mixes purely-snake_case backend fields.
 */
data class RepaymentEstimateDto(
    @SerializedName("debt_id") val debtId: Long,
    @SerializedName("repay_type") val repayType: String,
    @SerializedName("payment_method") val paymentMethod: String? = null,
    @SerializedName("amount_due") val amountDue: Double = 0.0,
    @SerializedName("principal_due") val principalDue: Double = 0.0,
    @SerializedName("interest_due") val interestDue: Double = 0.0,
    @SerializedName("penalty_fee") val penaltyFee: Double = 0.0,
    @SerializedName("discount_amount") val discountAmount: Double = 0.0,
    @SerializedName("reward_points_preview") val rewardPointsPreview: Int? = null,
    @SerializedName("currency") val currency: String = "VND",
    @SerializedName("debt_status_after_payment") val debtStatusAfterPayment: String? = null
) {
    fun toDomain(): RepaymentEstimate = RepaymentEstimate(
        debtId = debtId,
        repayType = if (repayType == RepayType.FULL_EARLY.apiValue) RepayType.FULL_EARLY else RepayType.MONTHLY,
        paymentMethod = paymentMethod,
        amountDue = amountDue,
        principalDue = principalDue,
        interestDue = interestDue,
        penaltyFee = penaltyFee,
        discountAmount = discountAmount,
        rewardPointsPreview = rewardPointsPreview,
        currency = currency,
        debtStatusAfterPayment = debtStatusAfterPayment
    )
}
