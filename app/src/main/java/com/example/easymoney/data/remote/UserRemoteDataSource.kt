package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ChangePasswordRequestDto
import com.example.easymoney.data.remote.dto.NotificationSettingsRequestDto
import com.example.easymoney.data.remote.dto.toDomain
import com.example.easymoney.data.remote.dto.toDto
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.ProfileCompletion
import com.example.easymoney.domain.model.UserProfile
import javax.inject.Inject

/**
 * Workflow #44 — Reference REMOTE data source. Maps DTO <-> domain và bọc lỗi mạng
 * giống pattern của [LoanRemoteDataSource].
 */
class UserRemoteDataSource @Inject constructor(
    private val apiService: UserApiService
) {
    suspend fun getProfile(): Resource<UserProfile> = safeCall {
        apiService.getProfile().let { response ->
            if (response.status == "success") {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(userFriendlyErrorMessage(response.message, "Get profile failed"))
            }
        }
    }

    suspend fun updateProfile(profile: UserProfile): Resource<UserProfile> = safeCall {
        apiService.updateProfile(profile.toDto()).let { response ->
            if (response.status == "success") {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(userFriendlyErrorMessage(response.message, "Update profile failed"))
            }
        }
    }

    suspend fun getProfileCompletion(): Resource<ProfileCompletion> = safeCall {
        apiService.getProfileCompletion().let { response ->
            if (response.status == "success") {
                Resource.Success(response.data.toDomain())
            } else {
                Resource.Error(userFriendlyErrorMessage(response.message, "Get profile completion failed"))
            }
        }
    }

    suspend fun updateNotificationSettings(enabled: Boolean): Resource<Unit> = safeCall {
        apiService.updateNotificationSettings(NotificationSettingsRequestDto(enabled))
            .toUnitResource("Update notification settings failed")
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit> = safeCall {
        apiService.changePassword(ChangePasswordRequestDto(oldPassword, newPassword))
            .toUnitResource("Change password failed")
    }

    private fun com.example.easymoney.data.remote.dto.ApiResponse<Unit>.toUnitResource(
        failMessage: String
    ): Resource<Unit> =
        if (status == "success") Resource.Success(Unit) else Resource.Error(userFriendlyErrorMessage(message, failMessage))

    private suspend fun <T> safeCall(block: suspend () -> Resource<T>): Resource<T> = try {
        block()
    } catch (e: Exception) {
        Resource.Error(userFriendlyErrorMessage(e))
    }
}
