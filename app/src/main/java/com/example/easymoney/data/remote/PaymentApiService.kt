package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.AddCardRequestDto
import com.example.easymoney.data.remote.dto.ApiResponse
import com.example.easymoney.data.remote.dto.BankDto
import com.example.easymoney.data.remote.dto.PaymentCardDto
import com.example.easymoney.data.remote.dto.WalletInfoDto
import com.example.easymoney.domain.model.QrPayment
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
    suspend fun getPaymentCards(): ApiResponse<List<PaymentCardDto>>

    // Workflow #75 — bank metadata for the add-card dropdowns.
    @GET("api/v1/payment/banks")
    suspend fun getBanks(): ApiResponse<List<BankDto>>

    @POST("api/v1/payment/cards")
    suspend fun addPaymentCard(@Body request: AddCardRequestDto): ApiResponse<Map<String, Any>>

    @DELETE("api/v1/payment/cards/{id}")
    suspend fun deletePaymentCard(@Path("id") cardId: String): ApiResponse<Map<String, Any>>

    @POST("api/v1/payment/cards/verify")
    suspend fun verifyCard(@Body request: AddCardRequestDto): ApiResponse<Map<String, Any>>

    @GET("api/v1/payment/wallet")
    suspend fun getWalletInfo(): ApiResponse<WalletInfoDto>

    @POST("api/v1/payment/topup")
    suspend fun topUp(@Body request: TopUpRequest): ApiResponse<Map<String, Any>>

    @POST("api/v1/payment/withdraw")
    suspend fun withdraw(@Body request: WithdrawRequest): ApiResponse<Map<String, Any>>

    @PATCH("api/v1/payment/auto-deduction")
    suspend fun toggleAutoDeduction(@Body request: AutoDeductionRequest): ApiResponse<Map<String, Any>>

    @POST("api/v1/payments/qr")
    suspend fun createQrPayment(@Body request: CreateQrRequest): ApiResponse<QrPayment>

    @GET("api/v1/payments/qr/{id}/status")
    suspend fun getQrPaymentStatus(@Path("id") qrPaymentId: String): ApiResponse<QrPayment>
}

data class TopUpRequest(val amount: Long, val cardId: String)
data class WithdrawRequest(val amount: Long, val cardId: String, val biometricToken: String?)
data class AutoDeductionRequest(val enabled: Boolean)
data class CreateQrRequest(val amount: Long)
