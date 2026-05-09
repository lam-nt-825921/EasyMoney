package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.*
import java.io.File

data class EligibilityResult(
    val isEligible: Boolean,
    val reasonCode: String? = null,
    val message: String? = null,
    val action: String? = null // NAVIGATE_PROFILE, SHOW_REJECT
)

interface LoanRepository {
    // Auth
    suspend fun login(request: LoginRequest): Resource<AuthToken>
    suspend fun register(request: RegisterRequest): Resource<AuthToken>
    suspend fun logout(): Resource<Unit>

    // Remembered Accounts
    suspend fun getRememberedAccounts(): Resource<List<RememberedAccount>>
    suspend fun getLastRememberedAccount(): Resource<RememberedAccount?>
    suspend fun saveRememberedAccount(account: RememberedAccount): Resource<Unit>
    suspend fun deleteRememberedAccount(phone: String): Resource<Unit>
    suspend fun clearAllRememberedAccounts(): Resource<Unit>

    suspend fun getLoanPackageById(id: String): Resource<LoanPackageModel>
    suspend fun getMyPackage(): Resource<LoanPackageModel>
    suspend fun getMyInfo(): Resource<MyInfoModel>
    suspend fun getLoanProviderInfo(): Resource<LoanProviderInfoModel>

    // Master Data
    suspend fun getLoanPackages(
        minAmount: Long? = null,
        maxAmount: Long? = null,
        tenor: Int? = null,
        eligibleOnly: Boolean = false
    ): Resource<List<LoanPackageModel>>

    suspend fun checkEligibility(packageId: String): Resource<EligibilityResult>
    
    suspend fun getMasterDataMetadata(): Resource<MasterDataMetadata>
    
    suspend fun getProvinces(): Resource<List<MasterDataItem>>
    suspend fun getDistricts(provinceId: String): Resource<List<MasterDataItem>>
    suspend fun getWards(districtId: String): Resource<List<MasterDataItem>>
    suspend fun getProfessions(): Resource<List<MasterDataItem>>
    suspend fun getPositions(): Resource<List<MasterDataItem>>
    suspend fun getEducationLevels(): Resource<List<MasterDataItem>>
    suspend fun getMaritalStatuses(): Resource<List<MasterDataItem>>
    suspend fun getRelationships(): Resource<List<MasterDataItem>>

    // Application
    suspend fun submitApplication(request: LoanApplicationRequest): Resource<Unit>

    // eKYC

    /**
     * Upload ảnh face + metadata tới backend
     */
    suspend fun captureFace(request: EkycCaptureRequest): Resource<EkycCaptureResponse>
    
    /**
     * Upload ảnh với custom file (fallback)
     */
    suspend fun captureFaceCustom(
        imageFile: File,
        metadataJson: String
    ): Resource<EkycCaptureResponse>

    /**
     * Lấy nội dung hợp đồng vay vốn theo ID khoản vay
     */
    suspend fun getContractContent(loanId: String): Resource<String>

    /**
     * Gửi mã OTP tới SĐT của người dùng (Backend tự xác định SĐT)
     */
    suspend fun sendOtp(purpose: String): Resource<Unit>

    /**
     * Xác thực mã OTP
     */
    suspend fun verifyOtp(otp: String): Resource<Unit>
}
