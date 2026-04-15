package com.example.easymoney.data.remote

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class LoanRemoteDataSource @Inject constructor(
    private val apiService: LoanApiService
) {
    suspend fun getMyPackage(): Resource<LoanPackageModel> {
        return try {
            val response = apiService.getMyPackage()
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(response.message ?: "Unknown error")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getMasterDataMetadata(): Resource<MasterDataMetadata> {
        return try {
            val response = apiService.getMasterDataMetadata()
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
                Resource.Error(response.message ?: "API Error")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getDistricts(provinceId: String): Resource<List<MasterDataItem>> {
        return try {
            val response = apiService.getDistricts(provinceId)
            Resource.Success(response.data.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error")
        }
    }

    suspend fun getWards(districtId: String): Resource<List<MasterDataItem>> {
        return try {
            val response = apiService.getWards(districtId)
            Resource.Success(response.data.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error")
        }
    }

    suspend fun captureFace(imageFile: File, metadataJson: String): Resource<EkycCaptureResponse> {
        return try {
            val imageBody = imageFile.asRequestBody("image/jpeg".toMediaType())
            val facePart = MultipartBody.Part.createFormData("face_image", imageFile.name, imageBody)
            
            val metadataBody = metadataJson.toRequestBody("application/json".toMediaType())
            
            val response = apiService.captureFace(facePart, metadataBody)
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(response.message ?: "Capture failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error during capture")
        }
    }

    suspend fun sendOtp(purpose: String): Resource<Unit> {
        return try {
            val response = apiService.sendOtp(OtpRequest(purpose))
            if (response.status == "success") Resource.Success(Unit)
            else Resource.Error(response.message ?: "OTP send failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun verifyOtp(otp: String, purpose: String): Resource<Unit> {
        return try {
            val response = apiService.verifyOtp(OtpVerifyRequest(otp, purpose))
            if (response.status == "success") Resource.Success(Unit)
            else Resource.Error(response.message ?: "OTP verify failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    suspend fun getNotifications(): Resource<List<NotificationDto>> {
        return try {
            val response = apiService.getNotifications()
            if (response.status == "success") Resource.Success(response.data)
            else Resource.Error(response.message ?: "Fetch notifications failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
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
            else Resource.Error(response.message ?: "Trigger failed")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    private fun com.example.easymoney.data.remote.dto.MasterDataItemDto.toDomain() = MasterDataItem(
        id = this.id,
        name = this.name,
        parentId = this.parentId
    )
}
