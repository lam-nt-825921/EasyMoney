package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.remote.UserRemoteDataSource
import com.example.easymoney.data.sample.SAMPLE_USER_PROFILE
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.ProfileCompletion
import com.example.easymoney.domain.model.UserProfile
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "DataSource"

class UserRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences,
    private val remoteDataSource: UserRemoteDataSource
) : UserRepository {

    private var profile: UserProfile = SAMPLE_USER_PROFILE

    /** Mock-mode current password used to validate change-password requests offline. */
    private var mockCurrentPassword: String = MOCK_DEFAULT_PASSWORD

    override suspend fun getProfile(): Resource<UserProfile> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "UserRepository.getProfile mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(300)
                Resource.Success(profile, isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.getProfile()
        }
    }

    override suspend fun getProfileCompletion(): Resource<ProfileCompletion> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "UserRepository.getProfileCompletion mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(200)
                Resource.Success(
                    ProfileCompletion(
                        isProfileCompleted = profile.isProfileCompleted,
                        canApplyLoan = profile.isProfileCompleted,
                        completionPercent = if (profile.isProfileCompleted) 100 else 75,
                        missingFields = profile.missingFields,
                        nextAction = if (profile.isProfileCompleted) "APPLY_LOAN" else "IDENTITY_VERIFICATION",
                        statusMessage = profile.statusMessage
                            ?: if (profile.isProfileCompleted) "Hồ sơ đã hoàn thiện, bạn có thể đăng ký vay." else "Bạn cần hoàn thiện hồ sơ trước khi đăng ký vay."
                    ),
                    isFromMock = true
                )
            }
            DataSourceMode.REMOTE -> remoteDataSource.getProfileCompletion()
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
            DataSourceMode.REMOTE -> remoteDataSource.updateProfile(updatedProfile)
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
            DataSourceMode.REMOTE -> remoteDataSource.updateNotificationSettings(enabled)
        }
    }

    override suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "UserRepository.changePassword mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(500)
                if (oldPassword != mockCurrentPassword) {
                    Resource.Error(MOCK_WRONG_OLD_PASSWORD)
                } else {
                    mockCurrentPassword = newPassword
                    Resource.Success(Unit, isFromMock = true)
                }
            }
            DataSourceMode.REMOTE -> remoteDataSource.changePassword(oldPassword, newPassword)
        }
    }
}

private const val MOCK_DEFAULT_PASSWORD = "123456"
private const val MOCK_WRONG_OLD_PASSWORD = "Mật khẩu hiện tại không đúng"
