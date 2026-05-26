package com.example.easymoney.data.sample

import com.example.easymoney.domain.model.AddressInfo
import com.example.easymoney.domain.model.IdentityVerificationStatus
import com.example.easymoney.domain.model.JobInfo
import com.example.easymoney.domain.model.PersonalInfo
import com.example.easymoney.domain.model.ProfileVerificationStatus
import com.example.easymoney.domain.model.UserProfile

val SAMPLE_USER_PROFILE: UserProfile = UserProfile(
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
        monthlyIncome = 25_000_000,
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
