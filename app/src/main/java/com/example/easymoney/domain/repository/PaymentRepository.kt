package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.QrPayment
import com.example.easymoney.domain.model.WalletInfo

interface PaymentRepository {
    suspend fun getPaymentCards(): Resource<List<PaymentCard>>
    suspend fun addPaymentCard(card: PaymentCard): Resource<Unit>
    suspend fun deletePaymentCard(cardId: String): Resource<Unit>

    suspend fun getWalletInfo(): Resource<WalletInfo>
    suspend fun topUp(amount: Long, cardId: String): Resource<Unit>
    suspend fun withdraw(amount: Long, cardId: String, biometricToken: String?): Resource<Unit>
    suspend fun toggleAutoDeduction(enabled: Boolean): Resource<Unit>

    // Workflow #36 — Card verification + QR payment contract.
    suspend fun verifyCard(card: PaymentCard): Resource<Unit>
    suspend fun createQrPayment(amount: Long): Resource<QrPayment>
    suspend fun getQrPaymentStatus(qrPaymentId: String): Resource<QrPayment>

    /**
     * Workflow #62 — gọi sau khi register/login/logout/account-switch để xóa
     * state thuộc về user trước (MOCK seed cards/balance, cache nội bộ).
     * REMOTE không có local state, hàm này no-op cho REMOTE — nhưng vẫn nên gọi
     * để guard trường hợp đổi mode lúc runtime.
     */
    fun clearUserScopedState()
}
