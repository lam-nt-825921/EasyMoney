package com.example.easymoney.domain.model

/** Workflow #36 — Trạng thái giao dịch QR. */
enum class QrPaymentStatus { PENDING, SUCCESS, FAILED, EXPIRED, CANCELLED }

data class QrPayment(
    val id: String,
    val qrContent: String,
    val amount: Long,
    val status: QrPaymentStatus,
    val expiresAt: Long
)
