package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.UserProfile
import kotlinx.coroutines.delay
import javax.inject.Inject

import com.example.easymoney.domain.model.*

class UserRepositoryImpl @Inject constructor() : UserRepository {
    private var profile = UserProfile(
        personalInfo = PersonalInfo(
            fullName = "Nguyễn Văn A",
            phoneNumber = "0987654321",
            gender = "Nam",
            dateOfBirth = "1995-05-20",
            nationalId = "001200012345",
            issueDate = "2021-12-01"
        ),
        addressInfo = AddressInfo(
            permanentAddress = "123 Đường Láng, Đống Đa, Hà Nội",
            currentAddress = "123 Đường Láng, Đống Đa, Hà Nội"
        ),
        jobInfo = JobInfo(
            jobTitle = "Kỹ sư phần mềm",
            monthlyIncome = 25000000,
            companyName = "Tech Corp"
        ),
        identityStatus = IdentityVerificationStatus(
            isFaceVerified = false,
            isNfcVerified = false,
            isBiometricEnabled = true
        ),
        verificationStatus = ProfileVerificationStatus.INCOMPLETE,
        statusMessage = "Vui lòng hoàn thiện eKYC để kích hoạt hạn mức 100 triệu"
    )

    override suspend fun getProfile(): Resource<UserProfile> {
        delay(300)
        return Resource.Success(profile)
    }

    override suspend fun updateProfile(updatedProfile: UserProfile): Resource<Unit> {
        delay(500)
        profile = updatedProfile
        return Resource.Success(Unit)
    }

    override suspend fun updateNotificationSettings(enabled: Boolean): Resource<Unit> {
        delay(200)
        return Resource.Success(Unit)
    }
}
