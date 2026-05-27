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
        eligibleOnly: Boolean = false,
        keyword: String = "",
        minInterest: Double? = null,
        maxInterest: Double? = null,
        hotOnly: Boolean = false,
        newOnly: Boolean = false,
        promotionalOnly: Boolean = false
    ): Resource<List<LoanPackageModel>>

    suspend fun checkEligibility(packageId: String): Resource<EligibilityResult>
    suspend fun getApplicableVouchers(packageId: String, loanAmount: Long): Resource<List<ApplicableVoucher>>
    suspend fun quoteLoan(packageId: String, request: LoanQuoteRequest): Resource<LoanQuote>
    suspend fun matchEkyc(packageId: String): Resource<EkycMatchResponse>
    
    // Workflow #30 — Master data nhận tham số ngôn ngữ; default "vi".
    suspend fun getMasterDataMetadata(lang: String = "vi"): Resource<MasterDataMetadata>

    suspend fun getProvinces(lang: String = "vi"): Resource<List<MasterDataItem>>
    suspend fun getDistricts(provinceId: String, lang: String = "vi"): Resource<List<MasterDataItem>>
    suspend fun getWards(districtId: String, lang: String = "vi"): Resource<List<MasterDataItem>>
    suspend fun getProfessions(lang: String = "vi"): Resource<List<MasterDataItem>>
    suspend fun getPositions(lang: String = "vi"): Resource<List<MasterDataItem>>
    suspend fun getEducationLevels(lang: String = "vi"): Resource<List<MasterDataItem>>
    suspend fun getMaritalStatuses(lang: String = "vi"): Resource<List<MasterDataItem>>
    suspend fun getRelationships(lang: String = "vi"): Resource<List<MasterDataItem>>

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
    suspend fun startEkycSession(supportsNfc: Boolean): Resource<String>
    suspend fun uploadIdentityDocument(): Resource<Unit>
    suspend fun submitNfcIdentity(sessionId: String?, nfcData: Map<String, String>): Resource<Unit>

    /**
     * Lấy nội dung hợp đồng vay vốn theo ID khoản vay
     */
    suspend fun getContractContent(loanId: String, lang: String = "vi"): Resource<String>

    /**
     * Gửi mã OTP tới SĐT của người dùng (Backend tự xác định SĐT)
     */
    suspend fun sendOtp(purpose: String): Resource<Unit>

    /**
     * Xác thực mã OTP
     */
    suspend fun verifyOtp(otp: String): Resource<Unit>

    // Workflow #12 — Loan management
    suspend fun getApprovedContracts(): Resource<List<LoanContractModel>>
    suspend fun cancelContract(contractId: String): Resource<Unit>
    suspend fun signContract(contractId: String): Resource<Unit>
    suspend fun getDebts(): Resource<List<LoanDebtModel>>
    suspend fun repayDebt(debtId: Long, repayType: RepayType, cardId: String? = null): Resource<Unit>
}
