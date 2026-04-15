package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.*
import com.example.easymoney.domain.model.EkycCaptureResponse
import com.example.easymoney.domain.model.LoanPackageModel
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface LoanApiService {

    @GET("api/v1/loan/package/my")
    suspend fun getMyPackage(): ApiResponse<LoanPackageModel>

    @GET("api/v1/master/metadata")
    suspend fun getMasterDataMetadata(): ApiResponse<MasterDataMetadataDto>

    @GET("api/v1/master/districts/{provinceId}")
    suspend fun getDistricts(@Path("provinceId") provinceId: String): ApiResponse<List<MasterDataItemDto>>

    @GET("api/v1/master/wards/{districtId}")
    suspend fun getWards(@Path("districtId") districtId: String): ApiResponse<List<MasterDataItemDto>>

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

    // --- Notifications ---
    @GET("api/v1/notifications")
    suspend fun getNotifications(): ApiResponse<List<NotificationDto>>

    @POST("api/v1/test/fcm/trigger")
    suspend fun triggerFcmTest(@Body request: FcmTestRequest): ApiResponse<Unit>
}

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
