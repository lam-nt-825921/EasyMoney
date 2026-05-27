package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.*
import com.example.easymoney.domain.model.ApplicableVoucher
import com.example.easymoney.domain.model.EkycMatchResponse
import com.example.easymoney.domain.model.EkycCaptureResponse
import com.example.easymoney.domain.model.LoanApplicationRequest
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

    @GET("api/v1/loan/package/my")
    suspend fun getMyPackage(): ApiResponse<LoanPackageModel>

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

data class OtpRequest(val purpose: String)
data class OtpVerifyRequest(val otp: String, val purpose: String)

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
    @SerializedName("expiresIn") val expiresIn: Int
)

data class NotificationDto(
    val id: Int,
    val title: String,
    val content: String,
    val type: String,
    val amount: Long? = null,
    val balanceAfter: Long? = null,
    val transactionCode: String? = null,
    val timestamp: Long,
    val isRead: Boolean
)
