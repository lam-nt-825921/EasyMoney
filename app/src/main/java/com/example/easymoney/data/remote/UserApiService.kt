package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ApiResponse
import com.example.easymoney.data.remote.dto.ChangePasswordRequestDto
import com.example.easymoney.data.remote.dto.NotificationSettingsRequestDto
import com.example.easymoney.data.remote.dto.ProfileCompletionDto
import com.example.easymoney.data.remote.dto.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

/**
 * Workflow #44 — User profile + account security endpoints.
 * Contract theo `documents/API_SPEC.md` §1/§5/§14.
 */
interface UserApiService {

    @GET("api/v1/user/profile")
    suspend fun getProfile(): ApiResponse<UserProfileDto>

    @GET("api/v1/user/profile/completion")
    suspend fun getProfileCompletion(): ApiResponse<ProfileCompletionDto>

    @PATCH("api/v1/user/profile")
    suspend fun updateProfile(@Body body: UserProfileDto): ApiResponse<UserProfileDto>

    @PATCH("api/v1/user/notification-settings")
    suspend fun updateNotificationSettings(@Body body: NotificationSettingsRequestDto): ApiResponse<Unit>

    @POST("api/v1/auth/change-password")
    suspend fun changePassword(@Body body: ChangePasswordRequestDto): ApiResponse<Unit>
}
