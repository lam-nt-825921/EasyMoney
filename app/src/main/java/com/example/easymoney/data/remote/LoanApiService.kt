package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.*
import com.example.easymoney.domain.model.ApplicableVoucher
import com.example.easymoney.domain.model.EkycMatchResponse
import com.example.easymoney.domain.model.EkycCaptureResponse
import com.example.easymoney.domain.model.LoanApplicationRequest
import com.example.easymoney.domain.model.LoanContractModel
import com.example.easymoney.domain.model.LoanDebtModel
import com.example.easymoney.domain.model.LoanPackageModel
import com.example.easymoney.domain.model.LoanQuote
import com.example.easymoney.domain.model.LoanQuoteRequest
import com.example.easymoney.domain.model.LoanSubmitResponse
import com.example.easymoney.domain.repository.EligibilityResult
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface LoanApiService {

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): ApiResponse<AuthTokenDto>

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): ApiResponse<AuthTokenDto>

    // Workflow #59 — backend trả về `data` là một mảng các package, không phải object đơn.
    @GET("api/v1/loan/package/my")
    suspend fun getMyPackage(): ApiResponse<List<LoanPackageModel>>

    @GET("api/v1/user/profile")
    suspend fun getProfile(): ApiResponse<UserProfileDto>

    @GET("api/v1/loan/package/{id}")
    suspend fun getLoanPackageById(@Path("id") id: String): ApiResponse<LoanPackageModel>

    @GET("api/v1/loan/package")
    suspend fun getLoanPackages(
        @Query("minAmount") minAmount: Long? = null,
        @Query("maxAmount") maxAmount: Long? = null,
        @Query("tenor") tenor: Int? = null,
        @Query("eligibleOnly") eligibleOnly: Boolean = false,
        @Query("keyword") keyword: String? = null,
        @Query("minInterest") minInterest: Double? = null,
        @Query("maxInterest") maxInterest: Double? = null,
        @Query("hotOnly") hotOnly: Boolean = false,
        @Query("newOnly") newOnly: Boolean = false,
        @Query("promotionalOnly") promotionalOnly: Boolean = false,
        @Query("lang") lang: String = "vi"
    ): ApiResponse<List<LoanPackageModel>>

    @POST("api/v1/loan/package/{id}/eligibility")
    suspend fun checkEligibility(@Path("id") id: String): ApiResponse<EligibilityResult>

    @GET("api/v1/loan/package/{id}/vouchers")
    suspend fun getApplicableVouchers(
        @Path("id") id: String,
        @Query("loanAmount") loanAmount: Long? = null
    ): ApiResponse<List<ApplicableVoucher>>

    @POST("api/v1/loan/package/{id}/quote")
    suspend fun quoteLoan(
        @Path("id") id: String,
        @Body request: LoanQuoteRequest
    ): ApiResponse<LoanQuote>

    @POST("api/v1/loan/applications")
    suspend fun submitApplication(@Body request: LoanApplicationRequest): ApiResponse<LoanSubmitResponse>

    @GET("api/v1/ekyc/match")
    suspend fun matchEkyc(@Query("packageId") packageId: String): ApiResponse<EkycMatchResponse>

    // Workflow #30 — Master data nhận query ?lang=vi|en
    @GET("api/v1/master/metadata")
    suspend fun getMasterDataMetadata(@Query("lang") lang: String = "vi"): ApiResponse<MasterDataMetadataDto>

    @GET("api/v1/master/districts/{provinceId}")
    suspend fun getDistricts(
        @Path("provinceId") provinceId: String,
        @Query("lang") lang: String = "vi"
    ): ApiResponse<List<MasterDataItemDto>>

    @GET("api/v1/master/wards/{districtId}")
    suspend fun getWards(
        @Path("districtId") districtId: String,
        @Query("lang") lang: String = "vi"
    ): ApiResponse<List<MasterDataItemDto>>

    // --- eKYC ---
    @Multipart
    @POST("api/v1/ekyc/capture/face")
    suspend fun captureFace(
        @Part faceImage: MultipartBody.Part,
        @Part("meta") metadata: RequestBody
    ): ApiResponse<EkycCaptureResponse>

    @POST("api/v1/ekyc/session")
    suspend fun createEkycSession(@Body request: EkycSessionRequest): ApiResponse<EkycSessionResponse>

    @POST("api/v1/ekyc/capture/face-base64")
    suspend fun captureFaceMock(@Body request: FaceCaptureMockRequest): ApiResponse<EkycCaptureResponse>

    @Multipart
    @POST("api/v1/ekyc/document/upload")
    suspend fun uploadIdentityDocument(
        @Part("meta") metadata: RequestBody
    ): ApiResponse<DocumentUploadResponse>

    @POST("api/v1/ekyc/document/nfc")
    suspend fun submitNfcIdentity(@Body request: DocumentNfcRequest): ApiResponse<DocumentUploadResponse>

    // --- OTP ---
    @POST("api/v1/otp/send")
    suspend fun sendOtp(@Body request: OtpRequest): ApiResponse<Unit>

    @POST("api/v1/otp/verify")
    suspend fun verifyOtp(@Body request: OtpVerifyRequest): ApiResponse<Unit>

    // --- Contracts ---
    @GET("api/v1/contracts/{contractId}")
    suspend fun getContractContent(
        @Path("contractId") contractId: String,
        @Query("lang") lang: String = "vi"
    ): ApiResponse<ContractContentDto>

    @GET("api/v1/loan/contracts/approved")
    suspend fun getApprovedContracts(): ApiResponse<List<LoanContractModel>>

    // Workflow #72 — canonical contract create/detail endpoints.
    @POST("api/v1/loan/contracts")
    suspend fun createContract(@Body request: CreateContractRequest): ApiResponse<LoanContractDetailDto>

    @GET("api/v1/loan/contracts/{contractId}")
    suspend fun getContractDetail(@Path("contractId") contractId: String): ApiResponse<LoanContractDetailDto>

    @POST("api/v1/loan/contracts/{contractId}/cancel")
    suspend fun cancelContract(@Path("contractId") contractId: String): ApiResponse<Map<String, Any>>

    // Workflow #72 — request a signing OTP (delivered to the device via FCM) separately from signing.
    @POST("api/v1/loan/contracts/{contractId}/sign/request-otp")
    suspend fun requestSignOtp(@Path("contractId") contractId: String): ApiResponse<Map<String, Any>>

    // Workflow #81 — final sign gửi kèm OTP + purpose trong body.
    @POST("api/v1/loan/contracts/{contractId}/sign")
    suspend fun signContract(
        @Path("contractId") contractId: String,
        @Body request: ContractSignRequest
    ): ApiResponse<Map<String, Any>>

    // Workflow #78 — mặc định ẩn nợ đã tất toán (status == PAID).
    @GET("api/v1/loan/debts")
    suspend fun getDebts(
        @Query("include_paid") includePaid: Boolean = false
    ): ApiResponse<List<LoanDebtModel>>

    @POST("api/v1/loan/debts/{debtId}/repay")
    suspend fun repayDebt(
        @Path("debtId") debtId: Long,
        @Body request: RepayDebtRequest
    ): ApiResponse<Map<String, Any>>

    // Workflow #71 — repayment estimate shown before confirming monthly/early settlement.
    @GET("api/v1/loan/debts/{debtId}/repayment-estimate")
    suspend fun getRepaymentEstimate(
        @Path("debtId") debtId: Long,
        @Query("repay_type") repayType: String,
        @Query("payment_method") paymentMethod: String? = null,
        @Query("card_id") cardId: String? = null
    ): ApiResponse<RepaymentEstimateDto>

    // --- Notifications ---
    @GET("api/v1/notifications")
    suspend fun getNotifications(): ApiResponse<List<NotificationDto>>

    @POST("api/v1/test/fcm/trigger")
    suspend fun triggerFcmTest(@Body request: FcmTestRequest): ApiResponse<Unit>

    // Workflow #46 — FCM token đăng ký + mark read
    @POST("api/v1/notifications/fcm-token")
    suspend fun registerFcmToken(@Body request: FcmTokenRequest): ApiResponse<Unit>

    @POST("api/v1/notifications/{id}/read")
    suspend fun markNotificationRead(@Path("id") id: Long): ApiResponse<Unit>

    @POST("api/v1/notifications/read-all")
    suspend fun markAllNotificationsRead(): ApiResponse<Unit>

    // Workflow #54 — clear all notifications on server
    @DELETE("api/v1/notifications/clear")
    suspend fun clearAllNotifications(): ApiResponse<Unit>
}

data class FcmTokenRequest(
    @SerializedName("fcmToken") val fcmToken: String
)

data class FcmTestRequest(
    @SerializedName("fcmToken")
    val fcmToken: String,
    @SerializedName("delaySeconds")
    val delaySeconds: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("body")
    val content: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("amount")
    val amount: Long? = null
)

data class OtpRequest(
    @SerializedName("phone") val phone: String,
    @SerializedName("purpose") val purpose: String
)
data class OtpVerifyRequest(val otp: String, val purpose: String)

/** Workflow #81 — body cho `POST .../sign`: OTP đã autofill + purpose. */
data class ContractSignRequest(
    @SerializedName("otp") val otp: String,
    @SerializedName("purpose") val purpose: String
)
data class RepayDebtRequest(
    val repayType: String,
    val cardId: String? = null,
    val paymentMethod: String = if (cardId.isNullOrBlank()) "WALLET" else "CARD"
)

data class EkycSessionRequest(
    val flow: String = "PROFILE_COMPLETION",
    val lang: String = "vi",
    val device: Map<String, Boolean>? = null
)

data class EkycSessionResponse(
    val sessionId: String,
    val status: String,
    val requiredSteps: List<String>,
    val completedSteps: List<String>,
    val availableDocumentMethods: List<String>
)

data class FaceCaptureMockRequest(
    val qualityScore: Double = 0.9,
    val precheckPassed: Boolean = true
)

data class DocumentUploadResponse(
    val documentId: String,
    val method: String,
    val status: String,
    val extracted: Map<String, Any> = emptyMap(),
    val matchResult: Map<String, Any> = emptyMap()
)

data class DocumentNfcRequest(
    val sessionId: String,
    val selfieCaptureId: String? = null,
    val documentType: String = "VN_CCCD",
    val nfc: Map<String, String>
)

data class ContractContentDto(
    @SerializedName("contractId") val contractId: String,
    @SerializedName("content") val content: String,
    @SerializedName("lang") val lang: String
)

data class LoginRequestDto(
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String
)

data class RegisterRequestDto(
    @SerializedName("phone") val phone: String,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("password") val password: String
)

data class AuthTokenDto(
    @SerializedName("accessToken") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String,
    @SerializedName("expiresIn") val expiresIn: Int,
    // Workflow #59 — backend trả kèm user profile sau login/register; cache để tránh round-trip thêm.
    @SerializedName("user") val user: com.example.easymoney.data.remote.dto.UserProfileDto? = null
)

