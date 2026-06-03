package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.NotificationDto
import com.example.easymoney.data.remote.dto.UserProfileDto
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.*
import com.example.easymoney.domain.repository.EligibilityResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

/**
 * Workflow #45/#59 — REMOTE data source dùng `safeApiCall` / `safeUnitApiCall` /
 * `mapSuccess` để xử lý nhất quán nullable `ApiResponse.data` (workflow #59).
 */
class LoanRemoteDataSource @Inject constructor(
    private val apiService: LoanApiService
) {
    suspend fun login(request: LoginRequest): Resource<AuthToken> =
        safeApiCall("Login failed") {
            apiService.login(LoginRequestDto(request.phone, request.password))
        }.mapSuccess { AuthToken(it.accessToken, it.refreshToken, it.expiresIn) }

    suspend fun register(request: RegisterRequest): Resource<AuthToken> =
        safeApiCall("Register failed") {
            apiService.register(RegisterRequestDto(request.phone, request.fullName, request.password))
        }.mapSuccess { AuthToken(it.accessToken, it.refreshToken, it.expiresIn) }

    suspend fun getMyPackage(): Resource<LoanPackageModel> {
        // Workflow #59 — backend trả về array; pick first hoặc Error rõ ràng nếu trống.
        return when (val result = safeApiCall("Fetch my package failed") { apiService.getMyPackage() }) {
            is Resource.Success -> result.data.firstOrNull()
                ?.let { Resource.Success(it) }
                ?: Resource.Error("Không có gói vay nào.")
            is Resource.Error -> Resource.Error(result.message, result.throwable)
            is Resource.Loading -> Resource.Loading
        }
    }

    suspend fun getProfile(): Resource<UserProfileDto> =
        safeApiCall("Fetch profile failed") { apiService.getProfile() }

    suspend fun getLoanPackageById(id: String): Resource<LoanPackageModel> =
        safeApiCall("Fetch loan package failed") { apiService.getLoanPackageById(id) }

    suspend fun getMasterDataMetadata(lang: String = "vi"): Resource<MasterDataMetadata> =
        safeApiCall("API Error") { apiService.getMasterDataMetadata(lang) }
            .mapSuccess { dto ->
                MasterDataMetadata(
                    version = dto.version,
                    expiredAt = dto.expiredAt,
                    provinces = dto.masterData.provinces.map { it.toDomain() },
                    professions = dto.masterData.professions.map { it.toDomain() },
                    positions = dto.masterData.positions.map { it.toDomain() },
                    educationLevels = dto.masterData.educationLevels.map { it.toDomain() },
                    maritalStatuses = dto.masterData.maritalStatuses.map { it.toDomain() },
                    relationships = dto.masterData.relationships.map { it.toDomain() }
                )
            }

    suspend fun getDistricts(provinceId: String, lang: String = "vi"): Resource<List<MasterDataItem>> =
        safeApiCall("Fetch districts failed") { apiService.getDistricts(provinceId, lang) }
            .mapSuccess { list -> list.map { it.toDomain() } }

    suspend fun getWards(districtId: String, lang: String = "vi"): Resource<List<MasterDataItem>> =
        safeApiCall("Fetch wards failed") { apiService.getWards(districtId, lang) }
            .mapSuccess { list -> list.map { it.toDomain() } }

    suspend fun captureFace(imageFile: File, metadataJson: String): Resource<EkycCaptureResponse> =
        safeApiCall("Capture failed") { apiService.captureFaceMock(FaceCaptureMockRequest()) }

    suspend fun sendOtp(purpose: String): Resource<Unit> =
        safeUnitApiCall("OTP send failed") { apiService.sendOtp(OtpRequest("0384473136", purpose)) }

    suspend fun verifyOtp(otp: String, purpose: String): Resource<Unit> =
        safeUnitApiCall("OTP verify failed") { apiService.verifyOtp(OtpVerifyRequest(otp, purpose)) }

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
    ): Resource<List<LoanPackageModel>> = safeApiCall("Fetch loan packages failed") {
        apiService.getLoanPackages(
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
    }

    suspend fun checkEligibility(packageId: String): Resource<EligibilityResult> =
        safeApiCall("Check eligibility failed") { apiService.checkEligibility(packageId) }

    suspend fun getApplicableVouchers(packageId: String, loanAmount: Long): Resource<List<ApplicableVoucher>> =
        safeApiCall("Fetch vouchers failed") { apiService.getApplicableVouchers(packageId, loanAmount) }

    suspend fun quoteLoan(packageId: String, request: LoanQuoteRequest): Resource<LoanQuote> =
        safeApiCall("Quote loan failed") { apiService.quoteLoan(packageId, request) }

    suspend fun submitApplication(request: LoanApplicationRequest): Resource<LoanSubmitResponse> =
        safeApiCall("Submit loan failed") { apiService.submitApplication(request) }

    suspend fun matchEkyc(packageId: String): Resource<EkycMatchResponse> =
        safeApiCall("Match eKYC failed") { apiService.matchEkyc(packageId) }

    suspend fun getContractContent(contractId: String, lang: String = "vi"): Resource<String> =
        safeApiCall("Fetch contract failed") { apiService.getContractContent(contractId, lang) }
            .mapSuccess { it.content }

    suspend fun startEkycSession(supportsNfc: Boolean): Resource<String> =
        safeApiCall("Create eKYC session failed") {
            apiService.createEkycSession(EkycSessionRequest(device = mapOf("supportsNfc" to supportsNfc)))
        }.mapSuccess { it.sessionId }

    suspend fun uploadIdentityDocument(): Resource<Unit> {
        val metadata = """{"flow":"PROFILE_COMPLETION","document_type":"VN_CCCD"}"""
            .toRequestBody("application/json".toMediaType())
        return when (val result = safeApiCall("Upload document failed") {
            apiService.uploadIdentityDocument(metadata)
        }) {
            is Resource.Success -> if (result.data.status == "VERIFIED") Resource.Success(Unit)
                else Resource.Error("Xác thực giấy tờ không thành công.")
            is Resource.Error -> Resource.Error(result.message, result.throwable)
            is Resource.Loading -> Resource.Loading
        }
    }

    suspend fun submitNfcIdentity(sessionId: String?, nfcData: Map<String, String>): Resource<Unit> {
        return when (val result = safeApiCall("NFC verification failed") {
            apiService.submitNfcIdentity(
                DocumentNfcRequest(
                    sessionId = sessionId.orEmpty().ifBlank { "mobile_${System.currentTimeMillis()}" },
                    nfc = nfcData
                )
            )
        }) {
            is Resource.Success -> if (result.data.status == "VERIFIED") Resource.Success(Unit)
                else Resource.Error("Xác thực NFC không thành công.")
            is Resource.Error -> Resource.Error(result.message, result.throwable)
            is Resource.Loading -> Resource.Loading
        }
    }

    suspend fun getApprovedContracts(): Resource<List<LoanContractModel>> =
        safeApiCall("Fetch contracts failed") { apiService.getApprovedContracts() }

    // Workflow #72 — contract create/detail.
    suspend fun createContract(applicationId: String): Resource<LoanContractDetail> =
        safeApiCall("Create contract failed") {
            apiService.createContract(com.example.easymoney.data.remote.dto.CreateContractRequest(applicationId))
        }.mapSuccess { it.toDomain() }

    suspend fun getContractDetail(contractId: String): Resource<LoanContractDetail> =
        safeApiCall("Fetch contract failed") { apiService.getContractDetail(contractId) }
            .mapSuccess { it.toDomain() }

    suspend fun cancelContract(contractId: String): Resource<Unit> =
        safeUnitApiCall("Cancel contract failed") { apiService.cancelContract(contractId) }

    // Workflow #81 — request OTP trả về otp + expires_at để autofill (không bắt nhập tay).
    suspend fun requestSignOtp(contractId: String): Resource<ContractOtpRequestResult> =
        safeApiCall("Request OTP failed") { apiService.requestSignOtp(contractId) }
            .mapSuccess { map ->
                ContractOtpRequestResult(
                    contractId = (map["contract_id"] as? String) ?: contractId,
                    otp = map["otp"] as? String,
                    expiresAt = (map["expires_at"] as? Number)?.toLong()
                )
            }

    // Workflow #81 — final sign gửi kèm OTP + purpose trong body.
    suspend fun signContract(contractId: String, otp: String): Resource<Unit> =
        safeUnitApiCall("Sign contract failed") {
            apiService.signContract(contractId, ContractSignRequest(otp, "SIGN_CONTRACT"))
        }

    suspend fun getDebts(): Resource<List<LoanDebtModel>> =
        safeApiCall("Fetch debts failed") { apiService.getDebts() }

    suspend fun repayDebt(debtId: Long, repayType: RepayType, cardId: String? = null): Resource<Unit> =
        safeUnitApiCall("Repay debt failed") {
            apiService.repayDebt(debtId, RepayDebtRequest(repayType.apiValue, cardId))
        }

    suspend fun getRepaymentEstimate(
        debtId: Long,
        repayType: RepayType,
        cardId: String? = null
    ): Resource<RepaymentEstimate> {
        val paymentMethod = if (cardId.isNullOrBlank()) "WALLET" else "CARD"
        return safeApiCall("Fetch repayment estimate failed") {
            apiService.getRepaymentEstimate(debtId, repayType.apiValue, paymentMethod, cardId)
        }.mapSuccess { it.toDomain() }
    }

    suspend fun getNotifications(): Resource<List<NotificationDto>> =
        safeApiCall("Fetch notifications failed") { apiService.getNotifications() }

    suspend fun triggerFcmTest(
        token: String,
        delay: Int,
        title: String,
        content: String,
        type: String,
        amount: Long?
    ): Resource<Unit> = safeUnitApiCall("Trigger failed") {
        apiService.triggerFcmTest(FcmTestRequest(token, delay, title, content, type, amount))
    }

    // Workflow #46 — FCM token + mark read
    suspend fun registerFcmToken(token: String): Resource<Unit> =
        safeUnitApiCall("Register FCM token failed") { apiService.registerFcmToken(FcmTokenRequest(token)) }

    suspend fun markNotificationRead(id: Long): Resource<Unit> =
        safeUnitApiCall("Mark read failed") { apiService.markNotificationRead(id) }

    suspend fun markAllNotificationsRead(): Resource<Unit> =
        safeUnitApiCall("Mark all read failed") { apiService.markAllNotificationsRead() }

    // Workflow #54 — server-side clear all
    suspend fun clearAllNotifications(): Resource<Unit> =
        safeUnitApiCall("Clear notifications failed") { apiService.clearAllNotifications() }

    private fun com.example.easymoney.data.remote.dto.MasterDataItemDto.toDomain() = MasterDataItem(
        id = this.id,
        name = this.name,
        parentId = this.parentId
    )
}
