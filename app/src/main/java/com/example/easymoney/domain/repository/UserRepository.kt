package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.ProfileCompletion
import com.example.easymoney.domain.model.UserProfile

interface UserRepository {
    suspend fun getProfile(): Resource<UserProfile>
    suspend fun getProfileCompletion(forceRefresh: Boolean = false): Resource<ProfileCompletion>
    fun getCachedProfileCompletion(): ProfileCompletion?
    suspend fun updateProfile(profile: UserProfile): Resource<Unit>
    suspend fun uploadAvatar(fileName: String, mimeType: String, bytes: ByteArray): Resource<UserProfile>
    suspend fun updateNotificationSettings(enabled: Boolean): Resource<Unit>
    suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit>
}
