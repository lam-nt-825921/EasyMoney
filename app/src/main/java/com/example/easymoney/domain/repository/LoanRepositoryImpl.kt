package com.example.easymoney.domain.repository

import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
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

    private fun isRemote() = appPreferences?.dataSourceMode == DataSourceMode.REMOTE && remoteDataSource != null

    // ========== Auth ==========
    override suspend fun login(request: LoginRequest): Resource<AuthToken> {
        if (isRemote()) return remoteDataSource?.login(request) ?: Resource.Error("Remote source error")
        
        delay(1000)
        return if (request.phone.isNotEmpty() && request.password == "123456") {
            Resource.Success(AuthToken("mock_access_token", "mock_refresh_token", 3600), isFromMock = true)
        } else {
            Resource.Error("Số điện thoại hoặc mật khẩu không chính xác.")
        }
    }

    override suspend fun register(request: RegisterRequest): Resource<AuthToken> {
        if (isRemote()) return remoteDataSource?.register(request) ?: Resource.Error("Remote source error")
        
        delay(1500)
        return Resource.Success(AuthToken("mock_access_token", "mock_refresh_token", 3600), isFromMock = true)
    }

    override suspend fun logout(): Resource<Unit> {
        delay(500)
        appPreferences?.clearAuthData()
        return Resource.Success(Unit)
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
            eligibleCreditScore = 600
        ),
        LoanPackageModel(
            id = "2",
            packageName = "Vay Linh Hoat",
            tenorRange = "3,6,9,12",
            minAmount = 3_000_000L,
            maxAmount = 50_000_000L,
            interest = 10.5,
            overdueCost = 4.0,
            eligibleCreditScore = 550
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
        organizationName = "Tổ chức tài chính EASY MONEY",
        hotline = "9999 9999",
        address = "114 Xuân Thủy, Phường Cầu Giấy, Thành phố Hà Nội."
    )

    override suspend fun getLoanPackageById(id: String): Resource<LoanPackageModel> {
        delay(300)

        if (id.isBlank()) {
            return Resource.Error(message = "Loan package id is empty")
        }

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
        delay(2000) // Tăng delay lên 2s để test skeleton

        return Resource.Success(data = mockMyInfo, isFromMock = true)
    }

    override suspend fun getLoanProviderInfo(): Resource<LoanProviderInfoModel> {
        delay(300)

        return Resource.Success(data = mockLoanProviderInfo, isFromMock = true)
    }

    // ========== Master Data Mock ==========
    override suspend fun getMasterDataMetadata(): Resource<MasterDataMetadata> {
        if (isRemote()) return remoteDataSource?.getMasterDataMetadata() ?: Resource.Error("Remote data source not available")
        
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

    override suspend fun getProvinces(): Resource<List<MasterDataItem>> {
        return when (val result = getMasterDataMetadata()) {
            is Resource.Success -> Resource.Success(result.data.provinces, isFromMock = true)
            is Resource.Error -> Resource.Error(result.message)
            else -> Resource.Loading
        }
    }

    override suspend fun getDistricts(provinceId: String): Resource<List<MasterDataItem>> {
        if (isRemote()) return remoteDataSource?.getDistricts(provinceId) ?: Resource.Error("Remote data source not available")
        
        delay(200)
        val data = when(provinceId) {
            "p_hn" -> listOf(MasterDataItem("d_th", "Quận Tây Hồ", provinceId), MasterDataItem("d_cg", "Quận Cầu Giấy", provinceId))
            "p_hcm" -> listOf(MasterDataItem("d_q1", "Quận 1", provinceId), MasterDataItem("d_q3", "Quận 3", provinceId))
            else -> emptyList()
        }
        return Resource.Success(data, isFromMock = true)
    }

    override suspend fun getWards(districtId: String): Resource<List<MasterDataItem>> {
        if (isRemote()) return remoteDataSource?.getWards(districtId) ?: Resource.Error("Remote data source not available")
        
        delay(200)
        val data = when(districtId) {
            "d_th" -> listOf(MasterDataItem("w_nt", "Phường Nhật Tân", districtId), MasterDataItem("w_pt", "Phường Phú Thượng", districtId))
            "d_cg" -> listOf(MasterDataItem("w_dh", "Phường Dịch Vọng Hậu", districtId))
            else -> listOf(MasterDataItem("w_default", "Phường Trung Tâm", districtId))
        }
        return Resource.Success(data, isFromMock = true)
    }

    override suspend fun getProfessions(): Resource<List<MasterDataItem>> {
        return when (val result = getMasterDataMetadata()) {
            is Resource.Success -> Resource.Success(result.data.professions, isFromMock = true)
            is Resource.Error -> Resource.Error(result.message)
            else -> Resource.Loading
        }
    }

    override suspend fun getPositions(): Resource<List<MasterDataItem>> {
        return when (val result = getMasterDataMetadata()) {
            is Resource.Success -> Resource.Success(result.data.positions, isFromMock = true)
            is Resource.Error -> Resource.Error(result.message)
            else -> Resource.Loading
        }
    }

    override suspend fun getEducationLevels(): Resource<List<MasterDataItem>> {
        return when (val result = getMasterDataMetadata()) {
            is Resource.Success -> Resource.Success(result.data.educationLevels, isFromMock = true)
            is Resource.Error -> Resource.Error(result.message)
            else -> Resource.Loading
        }
    }

    override suspend fun getMaritalStatuses(): Resource<List<MasterDataItem>> {
        return when (val result = getMasterDataMetadata()) {
            is Resource.Success -> Resource.Success(result.data.maritalStatuses, isFromMock = true)
            is Resource.Error -> Resource.Error(result.message)
            else -> Resource.Loading
        }
    }

    override suspend fun getRelationships(): Resource<List<MasterDataItem>> {
        return when (val result = getMasterDataMetadata()) {
            is Resource.Success -> Resource.Success(result.data.relationships, isFromMock = true)
            is Resource.Error -> Resource.Error(result.message)
            else -> Resource.Loading
        }
    }

    override suspend fun submitApplication(request: LoanApplicationRequest): Resource<Unit> {
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

    override suspend fun getContractContent(loanId: String): Resource<String> {
        delay(800)
        val content = """
            CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM
            Độc lập - Tự do - Hạnh phúc
            
            HỢP ĐỒNG CHO VAY TIÊU DÙNG
            Số: $loanId/HĐ-EM
            
            Bên cho vay: Tổ chức tài chính Easy Money
            Bên vay: Nguyen Duc Minh
            
            Nội dung chi tiết cho khoản vay có mã số: $loanId.
            
            Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. 
            
            Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of "de Finibus Bonorum et Malorum" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, "Lorem ipsum dolor sit amet..", comes from a line in section 1.10.32.
            
            ĐIỀU 1: ĐỐI TƯỢNG HỢP ĐỒNG
            Bên cho vay đồng ý cung cấp cho Bên vay một khoản tín dụng với mục đích tiêu dùng cá nhân cho mã hồ sơ $loanId.
            
            ĐIỀU 2: LÃI SUẤT VÀ PHÍ
            Lãi suất được áp dụng theo quy định của Tổ chức tài chính Easy Money tại từng thời điểm.
            
            ĐIỀU 3: QUYỀN VÀ NGHĨA VỤ
            Bên vay có trách nhiệm thanh toán đầy đủ và đúng hạn các khoản nợ gốc và lãi theo lịch trả nợ đã thỏa thuận.
            
            Hợp đồng này có hiệu lực kể từ ngày ký cho đến khi Bên vay hoàn thành mọi nghĩa vụ trả nợ.
        """.trimIndent()
        return Resource.Success(content, isFromMock = true)
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
            "precheck_reasons": [${request.precheckReasons.joinToString(",") { "\"$it\"" }}]
            ${if (request.faceBoundingBox != null) ""","face_bbox": ${request.faceBoundingBox}""" else ""}
            ${if (request.qualityScore != null) ""","quality_score": ${request.qualityScore}""" else ""}
        }"""
    }
}
