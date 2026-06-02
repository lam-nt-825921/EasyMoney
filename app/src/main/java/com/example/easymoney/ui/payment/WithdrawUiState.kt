package com.example.easymoney.ui.payment

import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.utils.UiText

data class WithdrawUiState(
    val amountText: String = "",
    val balance: Long = 0L,
    val cards: List<PaymentCard> = emptyList(),
    val selectedCardId: String? = null,
    val isSubmitting: Boolean = false,
    // Workflow #63 — error/success messages now use UiText so ViewModel emits string
    // resource ids; UI calls `.asString()` at the composable layer.
    val errorMessage: UiText? = null,
    val shouldNavigateToAddCard: Boolean = false,
    val successMessage: UiText? = null
) {
    val amountValue: Long? get() = amountText.toLongOrNull()
}
