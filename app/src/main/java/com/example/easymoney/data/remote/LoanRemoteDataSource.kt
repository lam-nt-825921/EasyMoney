package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.UserProfileDto
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.*
import com.example.easymoney.domain.repository.EligibilityResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class LoanRemoteDataSource @Inject constructor(
    private val apiService: LoanApiService
) {
    suspend fun login(request: LoginRequest): Resource<AuthToken> {
        return try {
            val response = apiService.login(LoginRequestDto(request.phone, request.password))
            if (response.status == "success") {
                val data = response.data
                Resource.Success(AuthToken(data.accessToken, data.refreshToken, data.expiresIn))
            } else Resource.Error(userFriendlyErrorMessage(response.message, "Login failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Login failed"))
        }
    }

    suspend fun register(request: RegisterRequest): Resource<AuthToken> {
        return try {
            val response = apiService.register(RegisterRequestDto(request.phone, request.fullName, request.password))
            if (response.status == "success") {
                val data = response.data
                Resource.Success(AuthToken(data.accessToken, data.refreshToken, data.expiresIn))
            } else Resource.Error(userFriendlyErrorMessage(response.message, "Register failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Register failed"))
        }
    }

    suspend fun getMyPackage(): Resource<LoanPackageModel> {
        return try {
            val response = apiService.getMyPackage()
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Unknown error"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e))
        }
    }

    suspend fun getProfile(): Resource<UserProfileDto> {
        return try {
            val response = apiService.getProfile()
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Fetch profile failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Fetch profile failed"))
        }
    }

    suspend fun getLoanPackageById(id: String): Resource<LoanPackageModel> {
        return try {
            val response = apiService.getLoanPackageById(id)
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Fetch loan package failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Fetch loan package failed"))
        }
    }

    suspend fun getMasterDataMetadata(lang: String = "vi"): Resource<MasterDataMetadata> {
        return try {
            val response = apiService.getMasterDataMetadata(lang)
            if (response.status == "success") {
                val dto = response.data
                val metadata = MasterDataMetadata(
                    version = dto.version,
                    expiredAt = dto.expiredAt,
                    provinces = dto.masterData.provinces.map { it.toDomain() },
                    professions = dto.masterData.professions.map { it.toDomain() },
                    positions = dto.masterData.positions.map { it.toDomain() },
                    educationLevels = dto.masterData.educationLevels.map { it.toDomain() },
                    maritalStatuses = dto.masterData.maritalStatuses.map { it.toDomain() },
                    relationships = dto.masterData.relationships.map { it.toDomain() }
                )
                Resource.Success(metadata)
            } else {
                Resource.Error(userFriendlyErrorMessage(response.message, "API Error"))
            }
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "API Error"))
        }
    }

    suspend fun getDistricts(provinceId: String, lang: String = "vi"): Resource<List<MasterDataItem>> {
        return try {
            val response = apiService.getDistricts(provinceId, lang)
            Resource.Success(response.data.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e))
        }
    }

    suspend fun getWards(districtId: String, lang: String = "vi"): Resource<List<MasterDataItem>> {
        return try {
            val response = apiService.getWards(districtId, lang)
            Resource.Success(response.data.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e))
        }
    }

    suspend fun captureFace(imageFile: File, metadataJson: String): Resource<EkycCaptureResponse> {
        return try {
            val response = apiService.captureFaceMock(FaceCaptureMockRequest())
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Capture failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Capture failed"))
        }
    }

    suspend fun sendOtp(purpose: String): Resource<Unit> {
        return try {
            val response = apiService.sendOtp(OtpRequest("0384473136",purpose))
            if (response.status == "success") Resource.Success(Unit)
            else Resource.Error(userFriendlyErrorMessage(response.message, "OTP send failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "OTP send failed"))
        }
    }

    suspend fun verifyOtp(otp: String, purpose: String): Resource<Unit> {
        return try {
            val response = apiService.verifyOtp(OtpVerifyRequest(otp, purpose))
            if (response.status == "success") Resource.Success(Unit)
            else Resource.Error(userFriendlyErrorMessage(response.message, "OTP verify failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "OTP verify failed"))
        }
    }

    suspend fun getLoanPackages(
        minAmount: Long?,
        maxAmount: Long?,
        tenor: Int?,
        eligibleOnly: Boolean,
        keyword: String,
        minInterest: Double?,
        maxInterest: Double?,
        hotOnly: Boolean,
        newOnly: Boolean,
        promotionalOnly: Boolean,
        lang: String = "vi"
    ): Resource<List<LoanPackageModel>> {
        return try {
            val response = apiService.getLoanPackages(
                minAmount = minAmount,
                maxAmount = maxAmount,
                tenor = tenor,
                eligibleOnly = eligibleOnly,
                keyword = keyword.ifBlank { null },
                minInterest = minInterest,
                maxInterest = maxInterest,
                hotOnly = hotOnly,
                newOnly = newOnly,
                promotionalOnly = promotionalOnly,
                lang = lang
            )
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Fetch loan packages failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Fetch loan packages failed"))
        }
    }

    suspend fun checkEligibility(packageId: String): Resource<EligibilityResult> {
        return try {
            val response = apiService.checkEligibility(packageId)
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Check eligibility failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Check eligibility failed"))
        }
    }

    suspend fun getApplicableVouchers(packageId: String, loanAmount: Long): Resource<List<ApplicableVoucher>> {
        return try {
            val response = apiService.getApplicableVouchers(packageId, loanAmount)
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Fetch vouchers failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Fetch vouchers failed"))
        }
    }

    suspend fun quoteLoan(packageId: String, request: LoanQuoteRequest): Resource<LoanQuote> {
        return try {
            val response = apiService.quoteLoan(packageId, request)
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Quote loan failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Quote loan failed"))
        }
    }

    suspend fun submitApplication(request: LoanApplicationRequest): Resource<LoanSubmitResponse> {
        return try {
            val response = apiService.submitApplication(request)
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Submit loan failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Submit loan failed"))
        }
    }

    suspend fun matchEkyc(packageId: String): Resource<EkycMatchResponse> {
        return try {
            val response = apiService.matchEkyc(packageId)
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Match eKYC failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Match eKYC failed"))
        }
    }

    suspend fun getContractContent(contractId: String, lang: String = "vi"): Resource<String> {
        return try {
            val response = apiService.getContractContent(contractId, lang)
            if (response.status == "success") Resource.Success(response.data.content)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Fetch contract failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Fetch contract failed"))
        }
    }

    suspend fun startEkycSession(supportsNfc: Boolean): Resource<String> {
        return try {
            val response = apiService.createEkycSession(
                EkycSessionRequest(device = mapOf("supportsNfc" to supportsNfc))
            )
            if (response.status == "success") Resource.Success(response.data.sessionId)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Create eKYC session failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Create eKYC session failed"))
        }
    }

    suspend fun uploadIdentityDocument(): Resource<Unit> = try {
        val metadata = """{"flow":"PROFILE_COMPLETION","document_type":"VN_CCCD"}"""
            .toRequestBody("application/json".toMediaType())
        val response = apiService.uploadIdentityDocument(metadata)
        if (response.status == "success" && response.data.status == "VERIFIED") {
            Resource.Success(Unit)
        } else {
            Resource.Error(userFriendlyErrorMessage(response.message ?: "Xác thực giấy tờ không thành công.", "Upload document failed"))
        }
    } catch (e: Exception) {
        Resource.Error(userFriendlyErrorMessage(e, "Upload document failed"))
    }

    suspend fun submitNfcIdentity(sessionId: String?, nfcData: Map<String, String>): Resource<Unit> = try {
        val response = apiService.submitNfcIdentity(
            DocumentNfcRequest(
                sessionId = sessionId.orEmpty().ifBlank { "mobile_${System.currentTimeMillis()}" },
                nfc = nfcData
            )
        )
        if (response.status == "success" && response.data.status == "VERIFIED") {
            Resource.Success(Unit)
        } else {
            Resource.Error(userFriendlyErrorMessage(response.message ?: "Xác thực NFC không thành công.", "NFC verification failed"))
        }
    } catch (e: Exception) {
        Resource.Error(userFriendlyErrorMessage(e, "NFC verification failed"))
    }

    suspend fun getApprovedContracts(): Resource<List<LoanContractModel>> {
        return try {
            val response = apiService.getApprovedContracts()
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Fetch contracts failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Fetch contracts failed"))
        }
    }

    suspend fun cancelContract(contractId: String): Resource<Unit> = unitCall("Cancel contract failed") {
        apiService.cancelContract(contractId)
    }

    suspend fun signContract(contractId: String): Resource<Unit> = unitCall("Sign contract failed") {
        apiService.signContract(contractId)
    }

    suspend fun getDebts(): Resource<List<LoanDebtModel>> {
        return try {
            val response = apiService.getDebts()
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Fetch debts failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Fetch debts failed"))
        }
    }

    suspend fun repayDebt(debtId: Long, repayType: RepayType, cardId: String? = null): Resource<Unit> = unitCall("Repay debt failed") {
        apiService.repayDebt(debtId, RepayDebtRequest(repayType.apiValue, cardId))
    }

    suspend fun getNotifications(): Resource<List<NotificationDto>> {
        return try {
            val response = apiService.getNotifications()
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Fetch notifications failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Fetch notifications failed"))
        }
    }

    suspend fun triggerFcmTest(
        token: String,
        delay: Int,
        title: String,
        content: String,
        type: String,
        amount: Long?
    ): Resource<Unit> {
        return try {
            val request = FcmTestRequest(token, delay, title, content, type, amount)
            val response = apiService.triggerFcmTest(request)
            if (response.status == "success") Resource.Success(Unit)
            else Resource.Error(userFriendlyErrorMessage(response.message, "Trigger failed"))
        } catch (e: Exception) {
            Resource.Error(userFriendlyErrorMessage(e, "Trigger failed"))
        }
    }

    // Workflow #46 — FCM token + mark read
    suspend fun registerFcmToken(token: String): Resource<Unit> = try {
        val response = apiService.registerFcmToken(FcmTokenRequest(token))
        if (response.status == "success") Resource.Success(Unit)
        else Resource.Error(userFriendlyErrorMessage(response.message, "Register FCM token failed"))
    } catch (e: Exception) {
        Resource.Error(userFriendlyErrorMessage(e, "Register FCM token failed"))
    }

    suspend fun markNotificationRead(id: Long): Resource<Unit> = try {
        val response = apiService.markNotificationRead(id)
        if (response.status == "success") Resource.Success(Unit)
        else Resource.Error(userFriendlyErrorMessage(response.message, "Mark read failed"))
    } catch (e: Exception) {
        Resource.Error(userFriendlyErrorMessage(e, "Mark read failed"))
    }

    suspend fun markAllNotificationsRead(): Resource<Unit> = try {
        val response = apiService.markAllNotificationsRead()
        if (response.status == "success") Resource.Success(Unit)
        else Resource.Error(userFriendlyErrorMessage(response.message, "Mark all read failed"))
    } catch (e: Exception) {
        Resource.Error(userFriendlyErrorMessage(e, "Mark all read failed"))
    }

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

    private fun com.example.easymoney.data.remote.dto.MasterDataItemDto.toDomain() = MasterDataItem(
        id = this.id,
        name = this.name,
        parentId = this.parentId
    )
}
