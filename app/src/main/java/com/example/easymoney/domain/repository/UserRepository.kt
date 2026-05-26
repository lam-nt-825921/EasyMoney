package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.UserProfile

interface UserRepository {
    suspend fun getProfile(): Resource<UserProfile>
    suspend fun updateProfile(profile: UserProfile): Resource<Unit>
    suspend fun updateNotificationSettings(enabled: Boolean): Resource<Unit>
    suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit>
}
