package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ChangePasswordRequestDto
import com.example.easymoney.data.remote.dto.NotificationSettingsRequestDto
import com.example.easymoney.data.remote.dto.toDomain
import com.example.easymoney.data.remote.dto.toDto
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.ProfileCompletion
import com.example.easymoney.domain.model.UserProfile
import okhttp3.MultipartBody
import javax.inject.Inject

/**
 * Workflow #44/#59 — Reference REMOTE data source. Maps DTO <-> domain qua
 * `safeApiCall`/`safeUnitApiCall` (đã xử lý nullable data sau workflow #59).
 */
class UserRemoteDataSource @Inject constructor(
    private val apiService: UserApiService
) {
    suspend fun getProfile(): Resource<UserProfile> =
        safeApiCall("Get profile failed") { apiService.getProfile() }
            .mapSuccess { it.toDomain() }

    suspend fun updateProfile(profile: UserProfile): Resource<UserProfile> =
        safeApiCall("Update profile failed") { apiService.updateProfile(profile.toDto()) }
            .mapSuccess { it.toDomain() }

    suspend fun uploadAvatar(avatar: MultipartBody.Part): Resource<UserProfile> =
        safeApiCall("Upload avatar failed") { apiService.uploadAvatar(avatar) }
            .mapSuccess { it.toDomain() }

    suspend fun getProfileCompletion(): Resource<ProfileCompletion> =
        safeApiCall("Get profile completion failed") { apiService.getProfileCompletion() }
            .mapSuccess { it.toDomain() }

    suspend fun updateNotificationSettings(enabled: Boolean): Resource<Unit> =
        safeUnitApiCall("Update notification settings failed") {
            apiService.updateNotificationSettings(NotificationSettingsRequestDto(enabled))
        }

    suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit> =
        safeUnitApiCall("Change password failed") {
            apiService.changePassword(ChangePasswordRequestDto(oldPassword, newPassword))
        }
}
