package com.example.easymoney.data.remote.dto

import com.example.easymoney.domain.model.LoanContractDetail
import com.google.gson.annotations.SerializedName

/**
 * Workflow #72 — DTO for the canonical contract create/detail endpoints.
 * Explicit @SerializedName because every field is snake_case on the backend.
 */
data class LoanContractDetailDto(
    @SerializedName("id") val id: String,
    @SerializedName("application_id") val applicationId: String? = null,
    @SerializedName("contract_number") val contractNumber: String? = null,
    @SerializedName("amount") val amount: Long = 0,
    @SerializedName("term_months") val termMonths: Int = 0,
    @SerializedName("interest_rate") val interestRate: Double = 0.0,
    @SerializedName("approved_at") val approvedAt: Long = 0,
    @SerializedName("status") val status: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("html_content") val htmlContent: String? = null,
    @SerializedName("otp_required") val otpRequired: Boolean = true
) {
    fun toDomain(): LoanContractDetail = LoanContractDetail(
        id = id,
        applicationId = applicationId,
        contractNumber = contractNumber,
        amount = amount,
        termMonths = termMonths,
        interestRate = interestRate,
        approvedAt = approvedAt,
        status = status,
        content = content.orEmpty(),
        htmlContent = htmlContent,
        otpRequired = otpRequired
    )
}

/** Workflow #72 — body for `POST /api/v1/loan/contracts`. */
data class CreateContractRequest(
    @SerializedName("application_id") val applicationId: String
)
