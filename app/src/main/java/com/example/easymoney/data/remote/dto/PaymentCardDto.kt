package com.example.easymoney.data.remote.dto

import com.example.easymoney.domain.model.PaymentCard
import com.google.gson.annotations.SerializedName

/**
 * Workflow #59 — backend trả về `balance` dưới dạng float (e.g. 14265851.0).
 * Frontend giữ domain model balance là Long (VND tròn); DTO này nhận Double rồi map.
 */
data class PaymentCardDto(
    @SerializedName("id") val id: String,
    @SerializedName("card_number") val cardNumber: String? = null,
    @SerializedName("card_type") val cardType: String? = null,
    @SerializedName("bank_name") val bankName: String? = null,
    @SerializedName("bank_id") val bankId: String? = null,
    @SerializedName("card_holder_name") val cardHolderName: String? = null,
    @SerializedName("expiry") val expiry: String? = null,
    @SerializedName("balance") val balance: Double? = null
)

fun PaymentCardDto.toDomain(): PaymentCard = PaymentCard(
    id = id,
    cardNumber = cardNumber.orEmpty(),
    cardType = cardType.orEmpty(),
    bankName = bankName.orEmpty(),
    bankId = bankId.orEmpty(),
    cardHolderName = cardHolderName.orEmpty(),
    expiry = expiry.orEmpty(),
    balance = (balance ?: 0.0).toLong()
)
