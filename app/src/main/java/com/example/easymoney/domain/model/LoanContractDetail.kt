package com.example.easymoney.domain.model

/**
 * Workflow #72 — detailed contract returned by `POST /api/v1/loan/contracts` and
 * `GET /api/v1/loan/contracts/{contract_id}`. Carries the signable content plus the
 * metadata the eSign screen needs (amount, term, whether an OTP is required to sign).
 */
data class LoanContractDetail(
    val id: String,
    val applicationId: String?,
    val contractNumber: String?,
    val amount: Long,
    val termMonths: Int,
    val interestRate: Double,
    val approvedAt: Long,
    val status: String?,
    val content: String,
    val htmlContent: String?,
    val otpRequired: Boolean
)
