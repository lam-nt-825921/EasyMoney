package com.example.easymoney.ui.loan.information.form

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.ProfileCompletion
import com.example.easymoney.domain.model.UserProfile
import com.example.easymoney.domain.repository.LoanRepositoryImpl
import com.example.easymoney.domain.repository.UserRepository
import com.example.easymoney.ui.theme.EasyMoneyTheme

/**
 * Preview cho màn hình điền thông tin vay vốn (Step 3).
 * Sử dụng repository mẫu để hiển thị dữ liệu địa chỉ thường trú.
 */
@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LoanInformationFormPreview() {
    val viewModel = remember {
        LoanInformationFormViewModel(LoanRepositoryImpl(null, null, null), PreviewUserRepository)
    }

    EasyMoneyTheme {
        LoanInformationFormScreen(
            onNextStep = { /* Mock next step */ },
            viewModel = viewModel
        )
    }
}

private object PreviewUserRepository : UserRepository {
    override suspend fun getProfile(): Resource<UserProfile> = Resource.Success(UserProfile(), isFromMock = true)
    override suspend fun getProfileCompletion(forceRefresh: Boolean): Resource<ProfileCompletion> =
        Resource.Success(
            ProfileCompletion(
                isProfileCompleted = false,
                canApplyLoan = false,
                completionPercent = 0,
                missingFields = emptyList(),
                nextAction = "UPDATE_PROFILE",
                statusMessage = ""
            ),
            isFromMock = true
        )
    override fun getCachedProfileCompletion(): ProfileCompletion? = null
    override suspend fun updateProfile(profile: UserProfile): Resource<Unit> = Resource.Success(Unit, isFromMock = true)
    override suspend fun uploadAvatar(fileName: String, mimeType: String, bytes: ByteArray): Resource<UserProfile> =
        Resource.Success(UserProfile(avatarUri = "mock://avatar/$fileName"), isFromMock = true)
    override suspend fun updateNotificationSettings(enabled: Boolean): Resource<Unit> = Resource.Success(Unit, isFromMock = true)
    override suspend fun changePassword(oldPassword: String, newPassword: String): Resource<Unit> = Resource.Success(Unit, isFromMock = true)
}
