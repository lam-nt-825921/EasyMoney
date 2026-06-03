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
    private var cachedProfileCompletion: ProfileCompletion? = null
    private var cachedProfileCompletionIsFromMock: Boolean = false

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

    override fun getCachedProfileCompletion(): ProfileCompletion? = cachedProfileCompletion

    override suspend fun getProfileCompletion(forceRefresh: Boolean): Resource<ProfileCompletion> {
        if (!forceRefresh) {
            cachedProfileCompletion?.let {
                return Resource.Success(it, isFromMock = cachedProfileCompletionIsFromMock)
            }
        }

        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "UserRepository.getProfileCompletion mode=$mode forceRefresh=$forceRefresh")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(200)
                val completion = deriveProfileCompletionFromProfile(profile)
                cachedProfileCompletion = completion
                cachedProfileCompletionIsFromMock = true
                Resource.Success(completion, isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                when (val result = remoteDataSource.getProfileCompletion()) {
                    is Resource.Success -> {
                        cachedProfileCompletion = result.data
                        cachedProfileCompletionIsFromMock = result.isFromMock
                        result
                    }
                    is Resource.Error -> {
                        Resource.Error(
                            message = result.message,
                            throwable = result.throwable,
                            data = cachedProfileCompletion
                        )
                    }
                    Resource.Loading -> result
                }
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
                updateCachedCompletionFromProfile(updatedProfile, isFromMock = true)
                Resource.Success(Unit, isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                when (val result = remoteDataSource.updateProfile(updatedProfile)) {
                    is Resource.Success -> {
                        // Workflow #85 — write-through cache so repository state phản ánh giá trị
                        // vừa lưu ngay lập tức (tránh hiển thị dữ liệu cũ khi quay lại màn edit).
                        profile = result.data
                        updateCachedCompletionFromProfile(result.data, isFromMock = false)
                        Resource.Success(Unit, isFromMock = false)
                    }
                    is Resource.Error -> Resource.Error<Unit>(
                        message = result.message,
                        throwable = result.throwable
                    )
                    Resource.Loading -> Resource.Loading
                }
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

    private fun deriveProfileCompletionFromProfile(profile: UserProfile): ProfileCompletion {
        val isIdentityDocumentVerified = profile.identityStatus.isIdentityDocumentVerified
        val isIdentityComplete = profile.identityStatus.isFaceVerified && isIdentityDocumentVerified
        val isCompleted = profile.isProfileCompleted || isIdentityComplete
        val missingFields = buildList {
            addAll(profile.missingFields)
            if (!profile.identityStatus.isFaceVerified && none { it == "FACE_VERIFICATION" }) {
                add("FACE_VERIFICATION")
            }
            if (!isIdentityDocumentVerified && none { it == "IDENTITY_DOCUMENT" }) {
                add("IDENTITY_DOCUMENT")
            }
        }
        val completionPercent = when {
            isCompleted -> 100
            profile.identityStatus.isFaceVerified || isIdentityDocumentVerified -> 75
            else -> 50
        }

        return ProfileCompletion(
            isProfileCompleted = isCompleted,
            canApplyLoan = isCompleted,
            completionPercent = completionPercent,
            missingFields = missingFields,
            nextAction = if (isCompleted) "APPLY_LOAN" else "IDENTITY_VERIFICATION",
            statusMessage = profile.statusMessage ?: if (isCompleted) {
                "Hồ sơ đã hoàn thiện, bạn có thể đăng ký vay."
            } else {
                "Bạn cần hoàn thiện xác thực khuôn mặt và Căn cước công dân trước khi đăng ký vay."
            }
        )
    }

    private fun updateCachedCompletionFromProfile(profile: UserProfile, isFromMock: Boolean) {
        cachedProfileCompletion = deriveProfileCompletionFromProfile(profile)
        cachedProfileCompletionIsFromMock = isFromMock
    }
}

private const val MOCK_DEFAULT_PASSWORD = "123456"
private const val MOCK_WRONG_OLD_PASSWORD = "Mật khẩu hiện tại không đúng"
