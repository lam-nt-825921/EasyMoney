package com.example.easymoney.data.remote

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.QrPayment
import com.example.easymoney.domain.model.WalletInfo
import javax.inject.Inject

/** Workflow #47 — REMOTE data source cho wallet/cards/topup/withdraw/QR. */
class PaymentRemoteDataSource @Inject constructor(
    private val apiService: PaymentApiService
) {
    suspend fun getPaymentCards(): Resource<List<PaymentCard>> =
        safeApiCall("Get cards failed") { apiService.getPaymentCards() }

    suspend fun addPaymentCard(card: PaymentCard): Resource<Unit> =
        unitCall("Add card failed") { apiService.addPaymentCard(card) }

    suspend fun deletePaymentCard(cardId: String): Resource<Unit> =
        unitCall("Delete card failed") { apiService.deletePaymentCard(cardId) }

    suspend fun verifyCard(card: PaymentCard): Resource<Unit> =
        unitCall("Verify card failed") { apiService.verifyCard(card) }

    suspend fun getWalletInfo(): Resource<WalletInfo> =
        safeApiCall("Get wallet failed") { apiService.getWalletInfo() }

    suspend fun topUp(amount: Long, cardId: String): Resource<Unit> =
        unitCall("Top up failed") { apiService.topUp(TopUpRequest(amount, cardId)) }

    suspend fun withdraw(amount: Long, cardId: String, biometricToken: String?): Resource<Unit> =
        unitCall("Withdraw failed") { apiService.withdraw(WithdrawRequest(amount, cardId, biometricToken)) }

    suspend fun toggleAutoDeduction(enabled: Boolean): Resource<Unit> =
        unitCall("Toggle auto deduction failed") { apiService.toggleAutoDeduction(AutoDeductionRequest(enabled)) }

    suspend fun createQrPayment(amount: Long): Resource<QrPayment> =
        safeApiCall("Create QR payment failed") { apiService.createQrPayment(CreateQrRequest(amount)) }

    suspend fun getQrPaymentStatus(qrPaymentId: String): Resource<QrPayment> =
        safeApiCall("Get QR status failed") { apiService.getQrPaymentStatus(qrPaymentId) }

    private suspend fun unitCall(
        failMessage: String,
        call: suspend () -> com.example.easymoney.data.remote.dto.ApiResponse<Map<String, Any>>
    ): Resource<Unit> = try {
        val response = call()
        if (response.status == "success") Resource.Success(Unit)
        else Resource.Error(userFriendlyErrorMessage(response.message, failMessage))
    } catch (e: Exception) {
        Resource.Error(userFriendlyErrorMessage(e, failMessage))
    }
}
