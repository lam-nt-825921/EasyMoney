package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ApiResponse
import com.example.easymoney.data.remote.dto.toDto
import com.example.easymoney.data.remote.dto.toDomain
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.AddCardOutcome
import com.example.easymoney.domain.model.AddCardRequest
import com.example.easymoney.domain.model.Bank
import com.example.easymoney.domain.model.PaymentCard
import com.example.easymoney.domain.model.QrPayment
import com.example.easymoney.domain.model.WalletInfo
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import retrofit2.HttpException
import javax.inject.Inject

/** Workflow #47/#59 — REMOTE data source cho wallet/cards/topup/withdraw/QR. */
class PaymentRemoteDataSource @Inject constructor(
    private val apiService: PaymentApiService
) {
    private val errorBodyParser = Gson()

    suspend fun getPaymentCards(): Resource<List<PaymentCard>> =
        safeApiCall("Get cards failed") { apiService.getPaymentCards() }
            .mapSuccess { list -> list.map { it.toDomain() } }

    // Workflow #75 — bank metadata for the add-card dropdowns.
    suspend fun getBanks(): Resource<List<Bank>> =
        safeApiCall("Get banks failed") { apiService.getBanks() }
            .mapSuccess { list -> list.map { it.toDomain() } }

    /**
     * Workflow #75 — verify then add a card, surfacing structured backend `field_errors`
     * so the UI can show them inline. Never fabricates a card on failure.
     */
    suspend fun addCard(request: AddCardRequest): AddCardOutcome = try {
        val dto = request.toDto()
        val verify = apiService.verifyCard(dto)
        outcomeIfError(verify) ?: run {
            val add = apiService.addPaymentCard(dto)
            outcomeIfError(add) ?: AddCardOutcome.Success
        }
    } catch (e: HttpException) {
        parseHttpError(e)
    } catch (e: Exception) {
        AddCardOutcome.Failure(userFriendlyErrorMessage(e, "Add card failed"))
    }

    private fun outcomeIfError(response: ApiResponse<*>): AddCardOutcome? {
        if (response.status == "success") return null
        val fieldErrors = response.fieldErrors
        return if (!fieldErrors.isNullOrEmpty()) {
            AddCardOutcome.FieldErrors(fieldErrors, response.message)
        } else {
            AddCardOutcome.Failure(userFriendlyErrorMessage(response.message, "Add card failed"))
        }
    }

    private fun parseHttpError(exception: HttpException): AddCardOutcome {
        val body = exception.response()?.errorBody()?.string().orEmpty()
        val parsed = runCatching { errorBodyParser.fromJson(body, CardErrorBody::class.java) }.getOrNull()
        val fieldErrors = parsed?.fieldErrors
        return if (!fieldErrors.isNullOrEmpty()) {
            AddCardOutcome.FieldErrors(fieldErrors, parsed.message)
        } else {
            AddCardOutcome.Failure(userFriendlyErrorMessage(exception, "Add card failed"))
        }
    }

    private data class CardErrorBody(
        val status: String? = null,
        val code: String? = null,
        val message: String? = null,
        @SerializedName("field_errors") val fieldErrors: Map<String, String>? = null
    )

    suspend fun deletePaymentCard(cardId: String): Resource<Unit> =
        safeUnitApiCall("Delete card failed") { apiService.deletePaymentCard(cardId) }

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
