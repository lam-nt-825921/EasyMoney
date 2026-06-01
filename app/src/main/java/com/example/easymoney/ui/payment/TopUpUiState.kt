package com.example.easymoney.ui.payment

import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.utils.UiText

// Workflow #63 — errorMessage / successMessage migrated từ String? sang UiText? để
// ViewModel có thể emit string resource thay vì literal VI hard-coded.
data class TopUpUiState(
    val amountText: String = "",
    val cards: List<PaymentCard> = emptyList(),
    val selectedCardId: String? = null,
    val isSubmitting: Boolean = false,
    val errorMessage: UiText? = null,
    val successMessage: UiText? = null
) {
    val amountValue: Long? get() = amountText.toLongOrNull()
}
