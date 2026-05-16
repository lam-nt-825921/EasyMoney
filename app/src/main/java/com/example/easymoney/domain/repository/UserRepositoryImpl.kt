package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.sample.SAMPLE_USER_PROFILE
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.UserProfile
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "DataSource"
private const val REMOTE_NOT_READY = "Endpoint REMOTE chưa sẵn sàng — vui lòng chuyển Sandbox sang MOCK"

class UserRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences
) : UserRepository {

    private var profile: UserProfile = SAMPLE_USER_PROFILE

    override suspend fun getProfile(): Resource<UserProfile> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "UserRepository.getProfile mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(300)
                Resource.Success(profile, isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire real GET /users/me endpoint
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }

    override suspend fun updateProfile(updatedProfile: UserProfile): Resource<Unit> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "UserRepository.updateProfile mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(500)
                profile = updatedProfile
                Resource.Success(Unit, isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire real PUT /users/me endpoint
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }

    override suspend fun updateNotificationSettings(enabled: Boolean): Resource<Unit> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "UserRepository.updateNotificationSettings mode=$mode enabled=$enabled")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(200)
                Resource.Success(Unit, isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_20): wire real PATCH /users/me/notification-settings
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }
}
