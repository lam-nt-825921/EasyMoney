package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanPackageModel
import com.example.easymoney.domain.model.LoanProviderInfoModel
import com.example.easymoney.domain.model.MyInfoModel
import com.example.easymoney.domain.model.EkycCaptureRequest
import com.example.easymoney.domain.model.EkycCaptureResponse
import com.example.easymoney.domain.model.LoanApplicationRequest
import com.example.easymoney.domain.model.MasterDataItem
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.UUID
import javax.inject.Inject

class LoanRepositoryImpl @Inject constructor() : LoanRepository {

    private val myPackageId = "1"

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
        organizationName = "To chuc tai chinh EASY MONEY",
        hotline = "9999 9999",
        address = "114 Xuan Thuy, Phuong Cau Giay, Thanh pho Ha Noi."
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
        delay(300)

        val packageData = mockLoanPackages.firstOrNull { it.id == myPackageId }
            ?: return Resource.Error(message = "My loan package not found")

        return Resource.Success(data = packageData, isFromMock = true)
    }

    override suspend fun getMyInfo(): Resource<MyInfoModel> {
        delay(300)

        return Resource.Success(data = mockMyInfo, isFromMock = true)
    }

    override suspend fun getLoanProviderInfo(): Resource<LoanProviderInfoModel> {
        delay(300)

        return Resource.Success(data = mockLoanProviderInfo, isFromMock = true)
    }

    // ========== Master Data Mock ==========
    override suspend fun getProvinces(): Resource<List<MasterDataItem>> {
        delay(300)
        return Resource.Success(listOf(
            MasterDataItem("p_hn", "TP Hà Nội"),
            MasterDataItem("p_hcm", "TP Hồ Chí Minh"),
            MasterDataItem("p_dn", "TP Đà Nẵng")
        ), isFromMock = true)
    }

    override suspend fun getDistricts(provinceId: String): Resource<List<MasterDataItem>> {
        delay(200)
        val data = when(provinceId) {
            "p_hn" -> listOf(MasterDataItem("d_th", "Quận Tây Hồ", provinceId), MasterDataItem("d_cg", "Quận Cầu Giấy", provinceId))
            "p_hcm" -> listOf(MasterDataItem("d_q1", "Quận 1", provinceId), MasterDataItem("d_q3", "Quận 3", provinceId))
            else -> emptyList()
        }
        return Resource.Success(data, isFromMock = true)
    }

    override suspend fun getWards(districtId: String): Resource<List<MasterDataItem>> {
        delay(200)
        val data = when(districtId) {
            "d_th" -> listOf(MasterDataItem("w_nt", "Phường Nhật Tân", districtId), MasterDataItem("w_pt", "Phường Phú Thượng", districtId))
            "d_cg" -> listOf(MasterDataItem("w_dh", "Phường Dịch Vọng Hậu", districtId))
            else -> listOf(MasterDataItem("w_default", "Phường Trung Tâm", districtId))
        }
        return Resource.Success(data, isFromMock = true)
    }

    override suspend fun getProfessions(): Resource<List<MasterDataItem>> {
        delay(200)
        return Resource.Success(listOf(
            MasterDataItem("p1", "Nhân viên văn phòng công ty"),
            MasterDataItem("p2", "Công chức nhà nước"),
            MasterDataItem("p3", "Công nhân"),
            MasterDataItem("p4", "Lao động phổ thông"),
            MasterDataItem("p5", "Khác")
        ), isFromMock = true)
    }

    override suspend fun getPositions(): Resource<List<MasterDataItem>> {
        delay(200)
        return Resource.Success(listOf(
            MasterDataItem("pos1", "Nhân viên/Chuyên viên"),
            MasterDataItem("pos2", "Tổ phó"),
            MasterDataItem("pos3", "Nhóm trưởng/Tổ trưởng"),
            MasterDataItem("pos4", "Quản lý/Trưởng phòng"),
            MasterDataItem("pos5", "Khác")
        ), isFromMock = true)
    }

    override suspend fun getEducationLevels(): Resource<List<MasterDataItem>> {
        delay(200)
        return Resource.Success(listOf(
            MasterDataItem("e1", "Không có chuyên môn"),
            MasterDataItem("e2", "Bằng trung cấp"),
            MasterDataItem("e3", "Bằng cao đẳng"),
            MasterDataItem("e4", "Bằng đại học trở lên")
        ), isFromMock = true)
    }

    override suspend fun getMaritalStatuses(): Resource<List<MasterDataItem>> {
        delay(200)
        return Resource.Success(listOf(
            MasterDataItem("m1", "Độc thân"),
            MasterDataItem("m2", "Đã kết hôn"),
            MasterDataItem("m3", "Ly hôn"),
            MasterDataItem("m4", "Ly thân"),
            MasterDataItem("m5", "Góa")
        ), isFromMock = true)
    }

    override suspend fun getRelationships(): Resource<List<MasterDataItem>> {
        delay(200)
        return Resource.Success(listOf(
            MasterDataItem("r1", "Ông/Bà"),
            MasterDataItem("r2", "Bố/Mẹ"),
            MasterDataItem("r3", "Vợ/Chồng"),
            MasterDataItem("r4", "Anh/Chị/Em ruột"),
            MasterDataItem("r5", "Bạn bè/Đồng nghiệp")
        ), isFromMock = true)
    }

    override suspend fun submitApplication(request: LoanApplicationRequest): Resource<Unit> {
        delay(1500)
        return Resource.Success(Unit, isFromMock = true)
    }

    // ========== eKYC Face Capture ==========
    override suspend fun captureFace(request: EkycCaptureRequest): Resource<EkycCaptureResponse> {
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
            
            // TODO: Call actual Retrofit service when available
            // val response = ekycService.captureFace(multipartBody)
            
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
        return try {
            val imageBody = imageFile.asRequestBody("image/jpeg".toMediaType())
            val metadataBody = metadataJson.toRequestBody("application/json".toMediaType())
            
            val multipartBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("face_image", imageFile.name, imageBody)
                .addFormDataPart("meta", metadataJson, metadataBody)
                .build()
            
            // TODO: Call actual Retrofit service when available
            // val response = ekycService.captureFace(multipartBody)
            
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
