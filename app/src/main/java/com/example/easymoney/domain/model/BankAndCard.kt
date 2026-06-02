package com.example.easymoney.domain.model

/**
 * Workflow #75 — bank metadata from `GET /api/v1/payment/banks`, used to populate the
 * bank dropdown and constrain the card-type dropdown.
 */
data class Bank(
    val id: String,
    val name: String,
    val shortName: String?,
    val binPrefixes: List<String> = emptyList(),
    val logoUrl: String? = null,
    val supportedCardTypes: List<String> = emptyList()
)

/** Workflow #75 — request payload for verify/add card. */
data class AddCardRequest(
    val bankId: String,
    val bankName: String,
    val cardType: String,
    val cardNumber: String,
    val cardHolderName: String,
    val expiryMonth: String,
    val expiryYear: String
)

/**
 * Workflow #75 — result of a verify/add card attempt. [FieldErrors] carries per-field backend
 * validation messages so the UI can show them inline without clearing the form.
 */
sealed interface AddCardOutcome {
    data object Success : AddCardOutcome
    data class FieldErrors(val fieldErrors: Map<String, String>, val message: String?) : AddCardOutcome
    data class Failure(val message: String) : AddCardOutcome
}
