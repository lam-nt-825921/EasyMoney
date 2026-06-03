package com.example.easymoney.data.remote.dto

import com.example.easymoney.domain.model.AddCardRequest
import com.example.easymoney.domain.model.Bank
import com.google.gson.annotations.SerializedName

/** Workflow #75 — DTO for `GET /api/v1/payment/banks`. */
data class BankDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("short_name") val shortName: String? = null,
    @SerializedName("bin_prefixes") val binPrefixes: List<String> = emptyList(),
    @SerializedName("logo_url") val logoUrl: String? = null,
    @SerializedName("supported_card_types") val supportedCardTypes: List<String> = emptyList()
) {
    fun toDomain(): Bank = Bank(
        id = id,
        name = name,
        shortName = shortName,
        binPrefixes = binPrefixes,
        logoUrl = logoUrl,
        supportedCardTypes = supportedCardTypes
    )
}

/**
 * Workflow #80 — canonical request body for verify/add card.
 * `expiry` is sent as `MM/YYYY`; `cvv` is a string; `card_number` is digits-only.
 */
data class AddCardRequestDto(
    @SerializedName("bank_id") val bankId: String,
    @SerializedName("bank_name") val bankName: String,
    @SerializedName("card_type") val cardType: String,
    @SerializedName("card_number") val cardNumber: String,
    @SerializedName("card_holder_name") val cardHolderName: String,
    @SerializedName("expiry") val expiry: String,
    @SerializedName("cvv") val cvv: String
)

fun AddCardRequest.toDto(): AddCardRequestDto = AddCardRequestDto(
    bankId = bankId,
    bankName = bankName,
    cardType = cardType,
    cardNumber = cardNumber.filter(Char::isDigit),
    cardHolderName = cardHolderName,
    expiry = expiry,
    cvv = cvv
)
