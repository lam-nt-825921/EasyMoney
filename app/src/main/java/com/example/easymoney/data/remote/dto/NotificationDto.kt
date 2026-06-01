package com.example.easymoney.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Workflow #54 — backend `NotificationDto` shape (snake_case).
 * `amount` / `balance_after` are parsed as Double because backend
 * repayment math can produce fractional VND (e.g. -983333.3333333334);
 * UI rounds/formats explicitly.
 */
data class NotificationDto(
    @SerializedName("id") val id: Long,
    @SerializedName("title") val title: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("category") val category: String? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("balance_after") val balanceAfter: Double? = null,
    @SerializedName("transaction_code") val transactionCode: String? = null,
    @SerializedName("target_id") val targetId: String? = null,
    @SerializedName("target_type") val targetType: String? = null,
    @SerializedName("timestamp") val timestamp: Long = 0L,
    @SerializedName("is_read") val isRead: Boolean = false
)
