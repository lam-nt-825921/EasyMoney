package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.sample.SAMPLE_APPROVED_CONTRACTS
import com.example.easymoney.domain.model.*
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import javax.inject.Inject

import com.example.easymoney.data.local.dao.RememberedAccountDao
import com.example.easymoney.data.local.entity.RememberedAccountEntity
import com.example.easymoney.data.remote.LoanRemoteDataSource
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.*
import kotlinx.coroutines.delay

class LoanRepositoryImpl @Inject constructor(
    private val remoteDataSource: LoanRemoteDataSource?,
    private val appPreferences: AppPreferences?,
    private val rememberedAccountDao: RememberedAccountDao?
) : LoanRepository {

    private fun isRemote(): Boolean {
        val remote = appPreferences?.dataSourceMode == DataSourceMode.REMOTE && remoteDataSource != null
        Log.d(TAG_DATA_SOURCE, "LoanRepository mode=${if (remote) "REMOTE" else "MOCK"}")
        return remote
    }

    private companion object {
        const val TAG_DATA_SOURCE = "DataSource"
    }

    // ========== Auth ==========
    override suspend fun login(request: LoginRequest): Resource<AuthToken> {
        if (isRemote()) {
            return when (val result = remoteDataSource?.login(request) ?: Resource.Error("Remote source error")) {
                is Resource.Success -> {
                    saveAuthToken(result.data)
                    result
                }
                is Resource.Error -> result
                Resource.Loading -> result
            }
        }
        
        delay(1000)
        return if (request.phone.isNotEmpty() && request.password == "123456") {
            val token = AuthToken("mock_access_token", "mock_refresh_token", 3600)
            saveAuthToken(token)
            Resource.Success(token, isFromMock = true)
        } else {
            Resource.Error("Số điện thoại hoặc mật khẩu không chính xác.")
        }
    }

    override suspend fun register(request: RegisterRequest): Resource<AuthToken> {
        if (isRemote()) {
            return when (val result = remoteDataSource?.register(request) ?: Resource.Error("Remote source error")) {
                is Resource.Success -> {
                    saveAuthToken(result.data)
                    result
                }
                is Resource.Error -> result
                Resource.Loading -> result
            }
        }
        
        delay(1500)
        val token = AuthToken("mock_access_token", "mock_refresh_token", 3600)
        saveAuthToken(token)
        return Resource.Success(token, isFromMock = true)
    }

    override suspend fun logout(): Resource<Unit> {
        delay(500)
        appPreferences?.clearAuthData()
        return Resource.Success(Unit)
    }

    private fun saveAuthToken(token: AuthToken) {
        appPreferences?.accessToken = token.accessToken
        appPreferences?.refreshToken = token.refreshToken
    }

    // ========== Remembered Accounts ==========
    override suspend fun getRememberedAccounts(): Resource<List<RememberedAccount>> {
        return try {
            val entities = rememberedAccountDao?.getAll() ?: emptyList()
            Resource.Success(entities.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Database error")
        }
    }

    override suspend fun getLastRememberedAccount(): Resource<RememberedAccount?> {
        return try {
            val entity = rememberedAccountDao?.getLastAccount()
            Resource.Success(entity?.toDomain())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Database error")
        }
    }

    override suspend fun saveRememberedAccount(account: RememberedAccount): Resource<Unit> {
        return try {
            rememberedAccountDao?.insert(RememberedAccountEntity.fromDomain(account))
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Database error")
        }
    }

    override suspend fun deleteRememberedAccount(phone: String): Resource<Unit> {
        return try {
            rememberedAccountDao?.deleteByPhone(phone)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Database error")
        }
    }

    override suspend fun clearAllRememberedAccounts(): Resource<Unit> {
        return try {
            rememberedAccountDao?.deleteAll()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Database error")
        }
    }

    private val myPackageId = "1"

    // Simple Cache for Prototype
    private var cachedMetadata: MasterDataMetadata? = null
    private var cacheExpirationTime: Long = 0

    private val mockLoanPackages = listOf(
        LoanPackageModel(
            id = "1",
            packageName = "Vay Nhanh",
            tenorRange = "6,12,18,24",
            minAmount = 6_000_000L,
            maxAmount = 100_000_000L,
            interest = 12.0,
            overdueCost = 5.0,
            eligibleCreditScore = 600,
            isHot = true,
            badges = listOf("HOT")
        ),
        LoanPackageModel(
            id = "2",
            packageName = "Vay Linh Hoat",
            tenorRange = "3,6,9,12",
            minAmount = 3_000_000L,
            maxAmount = 50_000_000L,
            interest = 10.5,
            overdueCost = 4.0,
            eligibleCreditScore = 550,
            isNew = true,
            isPromotional = true,
            badges = listOf("NEW", "PROMO")
        )
    )

    private val mockMyInfo = MyInfoModel(
        fullName = "Nguyen Duc Minh",
        gender = "Nam",
        dateOfBirth = "01/05/2005",
        phoneNumber = "0936-552-900",
        nationalId = "093201403413",
        issueDate = "03/11/2020",
        permanentProvince = "TP Hà Nội",
        permanentDistrict = "Quận Tây Hồ",
        permanentWard = "Phường Nhật Tân",
        permanentDetail = "1 Lạc Long Quân"
    )

    private val mockLoanProviderInfo = LoanProviderInfoModel(
        organizationName = "easyMoney",
        hotline = "9999 9999",
        address = "114 Xuân Thủy, Phường Cầu Giấy, Thành phố Hà Nội."
    )

    override suspend fun getLoanPackageById(id: String): Resource<LoanPackageModel> {
        if (id.isBlank()) {
            return Resource.Error(message = "Loan package id is empty")
        }

        if (isRemote()) {
            return remoteDataSource?.getLoanPackageById(id)
                ?: Resource.Error("Remote data source not available")
        }

        delay(300)

        val packageData = mockLoanPackages.firstOrNull { it.id == id }
            ?: return Resource.Error(message = "Loan package not found")

        return Resource.Success(data = packageData, isFromMock = true)
    }

    override suspend fun getMyPackage(): Resource<LoanPackageModel> {
        if (isRemote()) return remoteDataSource?.getMyPackage() ?: Resource.Error("Remote data source not available")
        
        delay(300)

        val packageData = mockLoanPackages.firstOrNull { it.id == myPackageId }
            ?: return Resource.Error(message = "My loan package not found")

        return Resource.Success(data = packageData, isFromMock = true)
    }

    override suspend fun getMyInfo(): Resource<MyInfoModel> {
        if (isRemote()) {
            val result = remoteDataSource?.getProfile() ?: Resource.Error("Remote source error")
            return when (result) {
                is Resource.Success -> Resource.Success(result.data.toMyInfoModel())
                is Resource.Error -> Resource.Error(result.message)
                Resource.Loading -> Resource.Loading
            }
        }

        delay(2000) // Tăng delay lên 2s để test skeleton

        return Resource.Success(data = mockMyInfo, isFromMock = true)
    }

    private fun com.example.easymoney.data.remote.dto.UserProfileDto.toMyInfoModel(): MyInfoModel {
        val p = personalInfo
        val a = addressInfo
        
        // Parse address parts if they are in the string "Ward, District, Province"
        val addressParts = a?.permanentAddress?.split(",")?.map { it.trim() }?.filter { it.isNotBlank() } ?: emptyList()
        val province = addressParts.getOrNull(addressParts.size - 1)
        val district = addressParts.getOrNull(addressParts.size - 2)
        val ward = addressParts.getOrNull(addressParts.size - 3)
        val detail = if (addressParts.size >= 4) addressParts.dropLast(3).joinToString(", ") else addressParts.firstOrNull()

        return MyInfoModel(
            fullName = p?.fullName.orEmpty(),
            gender = p?.gender.orEmpty(),
            dateOfBirth = p?.dateOfBirth.orEmpty(),
            phoneNumber = p?.phoneNumber.orEmpty(),
            nationalId = p?.nationalId.orEmpty(),
            issueDate = p?.issueDate.orEmpty(),
            permanentProvince = province,
            permanentDistrict = district,
            permanentWard = ward,
            permanentDetail = detail ?: a?.permanentAddress
        )
    }

    override suspend fun getLoanProviderInfo(): Resource<LoanProviderInfoModel> {
        if (isRemote()) {
            // Placeholder: Backend endpoint for provider info might not be ready, 
            // but we use the default "easyMoney" as requested.
            return Resource.Success(data = mockLoanProviderInfo, isFromMock = false)
        }

        delay(300)

        return Resource.Success(data = mockLoanProviderInfo, isFromMock = true)
    }

    override suspend fun getLoanPackages(
        minAmount: Long?,
        maxAmount: Long?,
        tenor: Int?,
        eligibleOnly: Boolean,
        keyword: String,
        minInterest: Double?,
        maxInterest: Double?,
        hotOnly: Boolean,
        newOnly: Boolean,
        promotionalOnly: Boolean
    ): Resource<List<LoanPackageModel>> {
        if (isRemote()) {
            return remoteDataSource?.getLoanPackages(
                minAmount = minAmount,
                maxAmount = maxAmount,
                tenor = tenor,
                eligibleOnly = eligibleOnly,
                keyword = keyword,
                minInterest = minInterest,
                maxInterest = maxInterest,
                hotOnly = hotOnly,
                newOnly = newOnly,
                promotionalOnly = promotionalOnly
            ) ?: Resource.Error("Remote data source not available")
        }

        delay(800)
        var filtered = mockLoanPackages.map { pkg ->
            // Mock eligibility based on ID for demo
            when (pkg.id) {
                "1" -> pkg.copy(isEligible = true)
                "2" -> pkg.copy(isEligible = false, ineligibilityReason = "MISSING_PROFILE")
                else -> pkg.copy(isEligible = false, ineligibilityReason = "LOW_CREDIT_SCORE")
            }
        }

        if (minAmount != null) filtered = filtered.filter { it.maxAmount >= minAmount }
        if (maxAmount != null) filtered = filtered.filter { it.minAmount <= maxAmount }
        if (tenor != null) filtered = filtered.filter { it.getTenorList().contains(tenor) }
        if (eligibleOnly) filtered = filtered.filter { it.isEligible }
        if (keyword.isNotBlank()) filtered = filtered.filter { it.packageName.contains(keyword, ignoreCase = true) }
        if (minInterest != null) filtered = filtered.filter { it.interest >= minInterest }
        if (maxInterest != null) filtered = filtered.filter { it.interest <= maxInterest }
        if (hotOnly) filtered = filtered.filter { it.isHot }
        if (newOnly) filtered = filtered.filter { it.isNew }
        if (promotionalOnly) filtered = filtered.filter { it.isPromotional }

        return Resource.Success(data = filtered, isFromMock = true)
    }

    override suspend fun checkEligibility(packageId: String): Resource<EligibilityResult> {
        if (isRemote()) {
            return remoteDataSource?.checkEligibility(packageId)
                ?: Resource.Error("Remote data source not available")
        }

        delay(1000)
        return when (packageId) {
            "1" -> Resource.Success(EligibilityResult(true))
            "2" -> Resource.Success(EligibilityResult(
                isEligible = false,
                reasonCode = "MISSING_PROFILE",
                message = "Bạn cần hoàn thiện thông tin nghề nghiệp và eKYC để tiếp tục.",
                action = "NAVIGATE_PROFILE"
            ))
            else -> Resource.Success(EligibilityResult(
                isEligible = false,
                reasonCode = "LOW_CREDIT_SCORE",
                message = "Rất tiếc, điểm tín dụng của bạn chưa đủ để đăng ký gói vay này.",
                action = "SHOW_REJECT"
            ))
        }
    }

    override suspend fun getApplicableVouchers(
        packageId: String,
        loanAmount: Long
    ): Resource<List<ApplicableVoucher>> {
        if (isRemote()) {
            return remoteDataSource?.getApplicableVouchers(packageId, loanAmount)
                ?: Resource.Error("Remote data source not available")
        }
        return Resource.Success(emptyList(), isFromMock = true)
    }

    override suspend fun quoteLoan(
        packageId: String,
        request: LoanQuoteRequest
    ): Resource<LoanQuote> {
        if (isRemote()) {
            return remoteDataSource?.quoteLoan(packageId, request)
                ?: Resource.Error("Remote data source not available")
        }

        val pkg = mockLoanPackages.firstOrNull { it.id == packageId }
            ?: return Resource.Error("Loan package not found")
        val interest = (request.loanAmount * (pkg.interest / 100.0) * request.tenorMonth / 12.0).toLong()
        val insurance = if (request.hasInsurance) (request.loanAmount * 0.01).toLong() else 0L
        val total = request.loanAmount + interest + insurance
        return Resource.Success(
            LoanQuote(
                packageId = packageId,
                loanAmount = request.loanAmount,
                tenorMonth = request.tenorMonth,
                hasInsurance = request.hasInsurance,
                originalInterestRate = pkg.interest,
                finalInterestRate = pkg.interest,
                monthlyPrincipal = request.loanAmount / request.tenorMonth.coerceAtLeast(1),
                monthlyInterest = interest / request.tenorMonth.coerceAtLeast(1),
                monthlyPayment = total / request.tenorMonth.coerceAtLeast(1),
                totalInterest = interest,
                insuranceFee = insurance,
                discountAmount = 0,
                totalPayment = total
            ),
            isFromMock = true
        )
    }

    override suspend fun matchEkyc(packageId: String): Resource<EkycMatchResponse> {
        if (isRemote()) {
            return remoteDataSource?.matchEkyc(packageId)
                ?: Resource.Error("Remote data source not available")
        }
        return Resource.Success(
            EkycMatchResponse(
                isMatched = true,
                canApplyLoan = true,
                message = "eKYC mock đã so khớp.",
                packageId = packageId,
                ekycMatchKey = "mock_ekyc_match_key"
            ),
            isFromMock = true
        )
    }

    // ========== Master Data Mock ==========
    override suspend fun getMasterDataMetadata(lang: String): Resource<MasterDataMetadata> {
        if (isRemote()) return remoteDataSource?.getMasterDataMetadata(lang) ?: Resource.Error("Remote data source not available")
        
        val currentTime = System.currentTimeMillis()

        // Check cache
        if (cachedMetadata != null && currentTime < cacheExpirationTime) {
            return Resource.Success(cachedMetadata!!, isFromMock = true)
        }

        delay(800) // Simulate network delay for first load or reload

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        // Expire in 1 hour for testing
        val expirationDate = Date(currentTime + 3600_000)
        val expirationString = sdf.format(expirationDate)

        val metadata = MasterDataMetadata(
            version = "2026.04.15.01",
            expiredAt = expirationString,
            provinces = listOf(
                MasterDataItem("p_hn", "TP Hà Nội"),
                MasterDataItem("p_hcm", "TP Hồ Chí Minh"),
                MasterDataItem("p_dn", "TP Đà Nẵng")
            ),
            professions = listOf(
                MasterDataItem("p1", "Nhân viên văn phòng công ty"),
                MasterDataItem("p2", "Công chức nhà nước"),
                MasterDataItem("p3", "Công nhân"),
                MasterDataItem("p4", "Lao động phổ thông"),
                MasterDataItem("p5", "Khác")
            ),
            positions = listOf(
                MasterDataItem("pos1", "Nhân viên/Chuyên viên"),
                MasterDataItem("pos2", "Tổ phó"),
                MasterDataItem("pos3", "Nhóm trưởng/Tổ trưởng"),
                MasterDataItem("pos4", "Quản lý/Trưởng phòng"),
                MasterDataItem("pos5", "Khác")
            ),
            educationLevels = listOf(
                MasterDataItem("e1", "Không có chuyên môn"),
                MasterDataItem("e2", "Bằng trung cấp"),
                MasterDataItem("e3", "Bằng cao đẳng"),
                MasterDataItem("e4", "Bằng đại học trở lên")
            ),
            maritalStatuses = listOf(
                MasterDataItem("m1", "Độc thân"),
                MasterDataItem("m2", "Đã kết hôn"),
                MasterDataItem("m3", "Ly hôn"),
                MasterDataItem("m4", "Ly thân"),
                MasterDataItem("m5", "Góa")
            ),
            relationships = listOf(
                MasterDataItem("r1", "Ông/Bà"),
                MasterDataItem("r2", "Bố/Mẹ"),
                MasterDataItem("r3", "Vợ/Chồng"),
                MasterDataItem("r4", "Anh/Chị/Em ruột"),
                MasterDataItem("r5", "Bạn bè/Đồng nghiệp")
            )
        )

        // Save to cache
        cachedMetadata = metadata
        cacheExpirationTime = expirationDate.time

        return Resource.Success(metadata, isFromMock = true)
    }

    override suspend fun getProvinces(lang: String): Resource<List<MasterDataItem>> {
        return when (val result = getMasterDataMetadata(lang)) {
            is Resource.Success -> Resource.Success(result.data.provinces, isFromMock = true)
            is Resource.Error -> Resource.Error(result.message)
            else -> Resource.Loading
        }
    }

    override suspend fun getDistricts(provinceId: String, lang: String): Resource<List<MasterDataItem>> {
        if (isRemote()) return remoteDataSource?.getDistricts(provinceId, lang) ?: Resource.Error("Remote data source not available")
        
        delay(200)
        val data = when(provinceId) {
            "p_hn" -> listOf(MasterDataItem("d_th", "Quận Tây Hồ", provinceId), MasterDataItem("d_cg", "Quận Cầu Giấy", provinceId))
            "p_hcm" -> listOf(MasterDataItem("d_q1", "Quận 1", provinceId), MasterDataItem("d_q3", "Quận 3", provinceId))
            else -> emptyList()
        }
        return Resource.Success(data, isFromMock = true)
    }

    override suspend fun getWards(districtId: String, lang: String): Resource<List<MasterDataItem>> {
        if (isRemote()) return remoteDataSource?.getWards(districtId, lang) ?: Resource.Error("Remote data source not available")
        
        delay(200)
        val data = when(districtId) {
            "d_th" -> listOf(MasterDataItem("w_nt", "Phường Nhật Tân", districtId), MasterDataItem("w_pt", "Phường Phú Thượng", districtId))
            "d_cg" -> listOf(MasterDataItem("w_dh", "Phường Dịch Vọng Hậu", districtId))
            else -> listOf(MasterDataItem("w_default", "Phường Trung Tâm", districtId))
        }
        return Resource.Success(data, isFromMock = true)
    }

    override suspend fun getProfessions(lang: String): Resource<List<MasterDataItem>> {
        return when (val result = getMasterDataMetadata(lang)) {
            is Resource.Success -> Resource.Success(result.data.professions, isFromMock = true)
            is Resource.Error -> Resource.Error(result.message)
            else -> Resource.Loading
        }
    }

    override suspend fun getPositions(lang: String): Resource<List<MasterDataItem>> {
        return when (val result = getMasterDataMetadata(lang)) {
            is Resource.Success -> Resource.Success(result.data.positions, isFromMock = true)
            is Resource.Error -> Resource.Error(result.message)
            else -> Resource.Loading
        }
    }

    override suspend fun getEducationLevels(lang: String): Resource<List<MasterDataItem>> {
        return when (val result = getMasterDataMetadata(lang)) {
            is Resource.Success -> Resource.Success(result.data.educationLevels, isFromMock = true)
            is Resource.Error -> Resource.Error(result.message)
            else -> Resource.Loading
        }
    }

    override suspend fun getMaritalStatuses(lang: String): Resource<List<MasterDataItem>> {
        return when (val result = getMasterDataMetadata(lang)) {
            is Resource.Success -> Resource.Success(result.data.maritalStatuses, isFromMock = true)
            is Resource.Error -> Resource.Error(result.message)
            else -> Resource.Loading
        }
    }

    override suspend fun getRelationships(lang: String): Resource<List<MasterDataItem>> {
        return when (val result = getMasterDataMetadata(lang)) {
            is Resource.Success -> Resource.Success(result.data.relationships, isFromMock = true)
            is Resource.Error -> Resource.Error(result.message)
            else -> Resource.Loading
        }
    }

    override suspend fun submitApplication(request: LoanApplicationRequest): Resource<Unit> {
        if (isRemote()) {
            return when (val result = remoteDataSource?.submitApplication(request)
                ?: Resource.Error("Remote data source not available")) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error -> Resource.Error(result.message)
                Resource.Loading -> Resource.Loading
            }
        }

        delay(1500)
        return Resource.Success(Unit, isFromMock = true)
    }

    // ========== eKYC Face Capture ==========
    override suspend fun captureFace(request: EkycCaptureRequest): Resource<EkycCaptureResponse> {
        if (isRemote()) {
            return remoteDataSource?.captureFace(request.imageFile, buildMetadataJson(request)) 
                ?: Resource.Error("Remote data source not available")
        }
        
        return try {
            // Build multipart body
            val imageBody = request.imageFile.asRequestBody("image/jpeg".toMediaType())
            
            val metadataJson = buildMetadataJson(request)
            val metadataBody = metadataJson.toRequestBody("application/json".toMediaType())
            
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("face_image", request.imageFile.name, imageBody)
                .addFormDataPart("meta", metadataJson, metadataBody)
                .build()
            
            // For now, mock success/failure (50/50)
            delay(1500)
            val isSuccess = (0..100).random() >= 50
            
            if (isSuccess) {
                val mockResponse = EkycCaptureResponse(
                    captureId = "capture-${System.currentTimeMillis()}",
                    status = "accepted",
                    nextStep = "face_matching",
                    message = "Ảnh chân dung được nhận"
                )
                Resource.Success(mockResponse)
            } else {
                Resource.Error(
                    message = "Lỗi nhận diện: Ảnh bị mờ hoặc không đủ ánh sáng. Vui lòng chụp lại.",
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Capture failed")
        }
    }

    override suspend fun captureFaceCustom(
        imageFile: File,
        metadataJson: String
    ): Resource<EkycCaptureResponse> {
        if (isRemote()) {
            return remoteDataSource?.captureFace(imageFile, metadataJson) 
                ?: Resource.Error("Remote data source not available")
        }
        
        return try {
            val imageBody = imageFile.asRequestBody("image/jpeg".toMediaType())
            val metadataBody = metadataJson.toRequestBody("application/json".toMediaType())
            
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("face_image", imageFile.name, imageBody)
                .addFormDataPart("meta", metadataJson, metadataBody)
                .build()
            
            // For now, mock success/failure (50/50)
            delay(1500)
            val isSuccess = (0..100).random() >= 50
            print("captureFaceCustom: isSuccess = $isSuccess")
            
            if (isSuccess) {
                val mockResponse = EkycCaptureResponse(
                    captureId = "capture-${System.currentTimeMillis()}",
                    status = "accepted",
                    nextStep = "face_matching",
                    message = "Ảnh chân dung được nhận"
                )
                Resource.Success(mockResponse, isFromMock = true)
            } else {
                Resource.Error(
                    message = "Lỗi nhận diện: Ảnh bị mờ hoặc không đủ ánh sáng. Vui lòng chụp lại.",
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Capture failed")
        }
    }

    override suspend fun startEkycSession(supportsNfc: Boolean): Resource<String> {
        if (isRemote()) {
            return remoteDataSource?.startEkycSession(supportsNfc)
                ?: Resource.Error("Remote data source not available")
        }
        return Resource.Success("mock_ekyc_session_${System.currentTimeMillis()}", isFromMock = true)
    }

    override suspend fun uploadIdentityDocument(): Resource<Unit> {
        if (isRemote()) {
            return remoteDataSource?.uploadIdentityDocument()
                ?: Resource.Error("Remote data source not available")
        }
        delay(500)
        return Resource.Success(Unit, isFromMock = true)
    }

    override suspend fun submitNfcIdentity(sessionId: String?, nfcData: Map<String, String>): Resource<Unit> {
        if (isRemote()) {
            return remoteDataSource?.submitNfcIdentity(sessionId, nfcData)
                ?: Resource.Error("Remote data source not available")
        }
        delay(500)
        return Resource.Success(Unit, isFromMock = true)
    }

    override suspend fun getContractContent(loanId: String, lang: String): Resource<String> {
        if (isRemote()) {
            return remoteDataSource?.getContractContent(loanId, lang)
                ?: Resource.Error("Remote data source not available")
        }

        delay(800)
        val content = if (lang.startsWith("en", ignoreCase = true)) {
            """
            EASY MONEY CONSUMER LOAN AGREEMENT
            Contract No.: $loanId/EM

            Lender: Easy Money Finance Company
            Borrower: Nguyen Duc Minh

            This agreement records the consumer loan approved for application code $loanId.

            ARTICLE 1: LOAN PURPOSE
            The lender agrees to provide the borrower with a consumer credit facility for lawful personal needs.

            ARTICLE 2: LOAN AMOUNT, TERM, INTEREST, AND FEES
            The approved amount, repayment term, interest rate, and applicable fees are displayed in the loan package and repayment schedule accepted by the borrower before signing.

            ARTICLE 3: REPAYMENT OBLIGATIONS
            The borrower is responsible for paying principal, interest, and fees fully and on time according to the agreed repayment schedule.

            ARTICLE 4: INFORMATION AND VERIFICATION
            The borrower confirms that submitted personal, contact, employment, income, and identity verification information is accurate. Easy Money may use this information to evaluate, manage, and service the loan as permitted by law.

            ARTICLE 5: CONTRACT EFFECT
            This agreement takes effect from the signing date and remains valid until the borrower completes all repayment obligations or the contract is closed under Easy Money policy.
            """.trimIndent()
        } else {
            """
            CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM
            Độc lập - Tự do - Hạnh phúc
            
            HỢP ĐỒNG CHO VAY TIÊU DÙNG
            Số: $loanId/HĐ-EM
            
            Bên cho vay: Tổ chức tài chính Easy Money
            Bên vay: Nguyen Duc Minh
            
            Nội dung chi tiết cho khoản vay có mã số: $loanId.
            
            ĐIỀU 1: ĐỐI TƯỢNG HỢP ĐỒNG
            Bên cho vay đồng ý cung cấp cho Bên vay một khoản tín dụng với mục đích tiêu dùng cá nhân cho mã hồ sơ $loanId.
            
            ĐIỀU 2: SỐ TIỀN VAY, THỜI HẠN, LÃI SUẤT VÀ PHÍ
            Số tiền vay, thời hạn trả nợ, lãi suất và phí áp dụng được thể hiện tại gói vay và lịch trả nợ mà Bên vay đã xác nhận trước khi ký.
            
            ĐIỀU 3: QUYỀN VÀ NGHĨA VỤ
            Bên vay có trách nhiệm thanh toán đầy đủ và đúng hạn các khoản nợ gốc và lãi theo lịch trả nợ đã thỏa thuận.

            ĐIỀU 4: THÔNG TIN VÀ XÁC THỰC
            Bên vay xác nhận các thông tin cá nhân, liên hệ, nghề nghiệp, thu nhập và định danh đã cung cấp là chính xác. Easy Money được sử dụng thông tin này để thẩm định, quản lý và phục vụ khoản vay theo quy định pháp luật.
            
            Hợp đồng này có hiệu lực kể từ ngày ký cho đến khi Bên vay hoàn thành mọi nghĩa vụ trả nợ.
            """.trimIndent()
        }
        return Resource.Success(content, isFromMock = true)
    }

    override suspend fun createContract(applicationId: String): Resource<LoanContractDetail> {
        if (isRemote()) {
            return remoteDataSource?.createContract(applicationId)
                ?: Resource.Error("Remote data source not available")
        }
        delay(500)
        return Resource.Success(mockContractDetail("CONTRACT_$applicationId", applicationId), isFromMock = true)
    }

    override suspend fun getContractDetail(contractId: String): Resource<LoanContractDetail> {
        if (isRemote()) {
            return remoteDataSource?.getContractDetail(contractId)
                ?: Resource.Error("Remote data source not available")
        }
        delay(500)
        return Resource.Success(mockContractDetail(contractId, applicationId = null), isFromMock = true)
    }

    override suspend fun requestSignOtp(contractId: String): Resource<Unit> {
        if (isRemote()) {
            return remoteDataSource?.requestSignOtp(contractId)
                ?: Resource.Error("Remote data source not available")
        }
        delay(600)
        return Resource.Success(Unit, isFromMock = true)
    }

    private fun mockContractDetail(contractId: String, applicationId: String?): LoanContractDetail {
        val sample = SAMPLE_APPROVED_CONTRACTS.firstOrNull()
        return LoanContractDetail(
            id = contractId,
            applicationId = applicationId,
            contractNumber = sample?.contractNumber ?: contractId,
            amount = sample?.amount ?: 20_000_000,
            termMonths = sample?.termMonths ?: 12,
            interestRate = sample?.interestRate ?: 1.5,
            approvedAt = sample?.approvedAt ?: System.currentTimeMillis(),
            status = "APPROVED",
            content = "Hợp đồng vay $contractId — nội dung mẫu (mock).",
            htmlContent = null,
            otpRequired = true
        )
    }

    override suspend fun sendOtp(purpose: String): Resource<Unit> {
        if (isRemote()) return remoteDataSource?.sendOtp(purpose) ?: Resource.Error("Remote data source not available")
        
        delay(1000) // Giả lập mạng
        return Resource.Success(Unit, isFromMock = true)
    }

    override suspend fun verifyOtp(otp: String): Resource<Unit> {
        if (isRemote()) return remoteDataSource?.verifyOtp(otp, "SIGN_CONTRACT") ?: Resource.Error("Remote data source not available")
        
        delay(1500)
        return if (otp == "123456") {
            Resource.Success(Unit, isFromMock = true)
        } else {
            Resource.Error("Mã OTP không chính xác. Vui lòng kiểm tra lại.")
        }
    }
    
    /**
     * Build metadata JSON string từ request
     */
    // Workflow #12 — Loan management
    private val cancelledContractIds = mutableSetOf<String>()

    override suspend fun getApprovedContracts(): Resource<List<LoanContractModel>> {
        if (isRemote()) {
            return remoteDataSource?.getApprovedContracts()
                ?: Resource.Error("Remote data source not available")
        }

        delay(500)
        val contracts = SAMPLE_APPROVED_CONTRACTS.map { c ->
            if (c.id in cancelledContractIds) c.copy(status = ContractStatus.CANCELLED) else c
        }
        return Resource.Success(contracts, isFromMock = true)
    }

    override suspend fun cancelContract(contractId: String): Resource<Unit> {
        if (isRemote()) {
            return remoteDataSource?.cancelContract(contractId)
                ?: Resource.Error("Remote data source not available")
        }

        delay(400)
        cancelledContractIds.add(contractId)
        return Resource.Success(Unit, isFromMock = true)
    }

    private val mockDebts = mutableListOf<LoanDebtModel>()

    override suspend fun signContract(contractId: String): Resource<Unit> {
        if (isRemote()) {
            return remoteDataSource?.signContract(contractId)
                ?: Resource.Error("Remote data source not available")
        }

        delay(500)
        cancelledContractIds.remove(contractId)
        if (mockDebts.none { it.applicationId == contractId }) {
            mockDebts.add(
                LoanDebtModel(
                    id = System.currentTimeMillis(),
                    applicationId = contractId,
                    totalAmount = 12_000_000,
                    remainingPrincipal = 10_000_000,
                    monthlyPayment = 1_000_000,
                    interestRate = 1.5,
                    totalMonths = 12,
                    monthsPaid = 0,
                    status = "ACTIVE",
                    createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).format(Date())
                )
            )
        }
        return Resource.Success(Unit, isFromMock = true)
    }

    override suspend fun getDebts(): Resource<List<LoanDebtModel>> {
        if (isRemote()) {
            return remoteDataSource?.getDebts()
                ?: Resource.Error("Remote data source not available")
        }

        delay(400)
        return Resource.Success(mockDebts.toList(), isFromMock = true)
    }

    override suspend fun repayDebt(debtId: Long, repayType: RepayType, cardId: String?): Resource<Unit> {
        if (isRemote()) {
            return remoteDataSource?.repayDebt(debtId, repayType, cardId)
                ?: Resource.Error("Remote data source not available")
        }

        delay(500)
        val index = mockDebts.indexOfFirst { it.id == debtId }
        if (index < 0) return Resource.Error("Khoản nợ không tồn tại.")
        val debt = mockDebts[index]
        mockDebts[index] = when (repayType) {
            RepayType.MONTHLY -> {
                val paid = debt.monthsPaid + 1
                debt.copy(
                    monthsPaid = paid,
                    remainingPrincipal = (debt.remainingPrincipal - debt.monthlyPayment).coerceAtLeast(0),
                    status = if (paid >= debt.totalMonths) "PAID" else debt.status
                )
            }
            RepayType.FULL_EARLY -> debt.copy(
                monthsPaid = debt.totalMonths,
                remainingPrincipal = 0,
                status = "PAID"
            )
        }
        return Resource.Success(Unit, isFromMock = true)
    }

    override suspend fun getRepaymentEstimate(
        debtId: Long,
        repayType: RepayType,
        cardId: String?
    ): Resource<RepaymentEstimate> {
        if (isRemote()) {
            return remoteDataSource?.getRepaymentEstimate(debtId, repayType, cardId)
                ?: Resource.Error("Remote data source not available")
        }

        delay(300)
        val debt = mockDebts.firstOrNull { it.id == debtId }
            ?: return Resource.Error("Khoản nợ không tồn tại.")
        val paymentMethod = if (cardId.isNullOrBlank()) "WALLET" else "CARD"
        val estimate = when (repayType) {
            RepayType.MONTHLY -> {
                val interest = debt.remainingPrincipal * (debt.interestRate / 100.0)
                val principal = (debt.monthlyPayment - interest).coerceAtLeast(0.0)
                RepaymentEstimate(
                    debtId = debtId,
                    repayType = repayType,
                    paymentMethod = paymentMethod,
                    amountDue = debt.monthlyPayment.toDouble(),
                    principalDue = principal,
                    interestDue = interest,
                    penaltyFee = 0.0,
                    discountAmount = 0.0,
                    rewardPointsPreview = (debt.monthlyPayment / 10_000).toInt(),
                    currency = "VND",
                    debtStatusAfterPayment = if (debt.monthsPaid + 1 >= debt.totalMonths) "PAID" else "ACTIVE"
                )
            }
            RepayType.FULL_EARLY -> {
                val interest = debt.remainingPrincipal * (debt.interestRate / 100.0)
                val penalty = debt.remainingPrincipal * 0.02
                val amountDue = debt.remainingPrincipal + interest + penalty
                RepaymentEstimate(
                    debtId = debtId,
                    repayType = repayType,
                    paymentMethod = paymentMethod,
                    amountDue = amountDue,
                    principalDue = debt.remainingPrincipal.toDouble(),
                    interestDue = interest,
                    penaltyFee = penalty,
                    discountAmount = 0.0,
                    rewardPointsPreview = (amountDue / 10_000).toInt(),
                    currency = "VND",
                    debtStatusAfterPayment = "PAID"
                )
            }
        }
        return Resource.Success(estimate, isFromMock = true)
    }

    private fun buildMetadataJson(request: EkycCaptureRequest): String {
        return """{
            "session_id": "${request.sessionId}",
            "flow_id": "${request.flowId}",
            "step": "${request.step}",
            "capture_ts": ${request.captureTimestamp},
            "device_model": "${request.deviceModel}",
            "os_version": "${request.osVersion}",
            "camera_lens": "${request.cameraLens}",
            "image_width": ${request.imageWidth},
            "image_height": ${request.imageHeight},
            "precheck_passed": ${request.precheckPassed},
            "precheckPassed": ${request.precheckPassed},
            "precheck_reasons": [${request.precheckReasons.joinToString(",") { "\"$it\"" }}]
            ${if (request.faceBoundingBox != null) ""","face_bbox": ${request.faceBoundingBox}""" else ""}
            ${if (request.qualityScore != null) ""","quality_score": ${request.qualityScore},"qualityScore": ${request.qualityScore}""" else ""}
        }"""
    }
}
