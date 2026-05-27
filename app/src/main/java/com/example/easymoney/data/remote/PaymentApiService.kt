package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ApiResponse
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.QrPayment
import com.example.easymoney.domain.model.WalletInfo
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Workflow #47 — Payment + QR endpoints. Frontend chỉ hiển thị trạng thái backend;
 * không tự xử lý tiền. QR có create intent + poll status.
 */
interface PaymentApiService {

    @GET("api/v1/payment/cards")
    suspend fun getPaymentCards(): ApiResponse<List<PaymentCard>>

    @POST("api/v1/payment/cards")
    suspend fun addPaymentCard(@Body card: PaymentCard): ApiResponse<Unit>

    @DELETE("api/v1/payment/cards/{id}")
    suspend fun deletePaymentCard(@Path("id") cardId: String): ApiResponse<Unit>

    @POST("api/v1/payment/cards/verify")
    suspend fun verifyCard(@Body card: PaymentCard): ApiResponse<Unit>

    @GET("api/v1/payment/wallet")
    suspend fun getWalletInfo(): ApiResponse<WalletInfo>

    @POST("api/v1/payment/topup")
    suspend fun topUp(@Body request: TopUpRequest): ApiResponse<Unit>

    @POST("api/v1/payment/withdraw")
    suspend fun withdraw(@Body request: WithdrawRequest): ApiResponse<Unit>

    @PATCH("api/v1/payment/auto-deduction")
    suspend fun toggleAutoDeduction(@Body request: AutoDeductionRequest): ApiResponse<Unit>

    @POST("api/v1/payments/qr")
    suspend fun createQrPayment(@Body request: CreateQrRequest): ApiResponse<QrPayment>

    @GET("api/v1/payments/qr/{id}/status")
    suspend fun getQrPaymentStatus(@Path("id") qrPaymentId: String): ApiResponse<QrPayment>
}

data class TopUpRequest(val amount: Long, val cardId: String)
data class WithdrawRequest(val amount: Long, val cardId: String, val biometricToken: String?)
data class AutoDeductionRequest(val enabled: Boolean)
data class CreateQrRequest(val amount: Long)
