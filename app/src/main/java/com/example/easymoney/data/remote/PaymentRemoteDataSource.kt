package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.toDomain
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.QrPayment
import com.example.easymoney.domain.model.WalletInfo
import javax.inject.Inject

/** Workflow #47/#59 — REMOTE data source cho wallet/cards/topup/withdraw/QR. */
class PaymentRemoteDataSource @Inject constructor(
    private val apiService: PaymentApiService
) {
    suspend fun getPaymentCards(): Resource<List<PaymentCard>> =
        safeApiCall("Get cards failed") { apiService.getPaymentCards() }
            .mapSuccess { list -> list.map { it.toDomain() } }

    suspend fun addPaymentCard(card: PaymentCard): Resource<Unit> =
        safeUnitApiCall("Add card failed") { apiService.addPaymentCard(card) }

    suspend fun deletePaymentCard(cardId: String): Resource<Unit> =
        safeUnitApiCall("Delete card failed") { apiService.deletePaymentCard(cardId) }

    suspend fun verifyCard(card: PaymentCard): Resource<Unit> =
        safeUnitApiCall("Verify card failed") { apiService.verifyCard(card) }

    suspend fun getWalletInfo(): Resource<WalletInfo> =
        safeApiCall("Get wallet failed") { apiService.getWalletInfo() }
            .mapSuccess { it.toDomain() }

    suspend fun topUp(amount: Long, cardId: String): Resource<Unit> =
        safeUnitApiCall("Top up failed") { apiService.topUp(TopUpRequest(amount, cardId)) }

    suspend fun withdraw(amount: Long, cardId: String, biometricToken: String?): Resource<Unit> =
        safeUnitApiCall("Withdraw failed") {
            apiService.withdraw(WithdrawRequest(amount, cardId, biometricToken))
        }

    suspend fun toggleAutoDeduction(enabled: Boolean): Resource<Unit> =
        safeUnitApiCall("Toggle auto deduction failed") {
            apiService.toggleAutoDeduction(AutoDeductionRequest(enabled))
        }

    suspend fun createQrPayment(amount: Long): Resource<QrPayment> =
        safeApiCall("Create QR payment failed") { apiService.createQrPayment(CreateQrRequest(amount)) }

    suspend fun getQrPaymentStatus(qrPaymentId: String): Resource<QrPayment> =
        safeApiCall("Get QR status failed") { apiService.getQrPaymentStatus(qrPaymentId) }
}
