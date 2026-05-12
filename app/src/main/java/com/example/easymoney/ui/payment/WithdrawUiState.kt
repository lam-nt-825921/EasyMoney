package com.example.easymoney.ui.payment

import com.example.easymoney.domain.model.PaymentCard

data class WithdrawUiState(
    val amountText: String = "",
    val balance: Long = 0L,
    val cards: List<PaymentCard> = emptyList(),
    val selectedCardId: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val amountValue: Long? get() = amountText.toLongOrNull()
}
