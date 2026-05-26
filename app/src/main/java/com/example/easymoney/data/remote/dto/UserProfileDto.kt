package com.example.easymoney.data.remote.dto

import com.example.easymoney.domain.model.AddressInfo
import com.example.easymoney.domain.model.ContactInfo
import com.example.easymoney.domain.model.IdentityVerificationStatus
import com.example.easymoney.domain.model.JobInfo
import com.example.easymoney.domain.model.PersonalInfo
import com.example.easymoney.domain.model.ProfileVerificationStatus
import com.example.easymoney.domain.model.UserProfile

/**
 * Workflow #44 — User profile transport DTO. Gson dùng LOWER_CASE_WITH_UNDERSCORES
 * (xem NetworkModule) nên field camelCase sẽ map sang snake_case JSON.
 */
data class UserProfileDto(
    val avatarUri: String? = null,
    val personalInfo: PersonalInfoDto? = null,
    val addressInfo: AddressInfoDto? = null,
    val jobInfo: JobInfoDto? = null,
    val contactInfo: ContactInfoDto? = null,
    val education: String? = null,
    val maritalStatus: String? = null,
    val identityStatus: IdentityStatusDto? = null,
    val verificationStatus: String? = null,
    val statusMessage: String? = null
)

data class PersonalInfoDto(
    val fullName: String? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val phoneNumber: String? = null,
    val nationalId: String? = null,
    val issueDate: String? = null
)

data class AddressInfoDto(
    val permanentAddress: String? = null,
    val currentAddress: String? = null
)

data class JobInfoDto(
    val jobTitle: String? = null,
    val monthlyIncome: Long? = null,
    val companyName: String? = null,
    val position: String? = null
)

data class ContactInfoDto(
    val contactName: String? = null,
    val relationship: String? = null,
    val phoneNumber: String? = null
)

data class IdentityStatusDto(
    val isFaceVerified: Boolean? = null,
    val isNfcVerified: Boolean? = null,
    val isDocumentUploadVerified: Boolean? = null,
    val isBiometricEnabled: Boolean? = null
)

data class ChangePasswordRequestDto(
    val oldPassword: String,
    val newPassword: String
)

data class NotificationSettingsRequestDto(
    val enabled: Boolean
)

// ---- Mapping DTO -> domain (an toàn null) ----

fun UserProfileDto.toDomain(): UserProfile = UserProfile(
    avatarUri = avatarUri.orEmpty(),
    personalInfo = personalInfo?.toDomain() ?: PersonalInfo(),
    addressInfo = addressInfo?.toDomain() ?: AddressInfo(),
    jobInfo = jobInfo?.toDomain() ?: JobInfo(),
    contactInfo = contactInfo?.toDomain() ?: ContactInfo(),
    education = education.orEmpty(),
    maritalStatus = maritalStatus.orEmpty(),
    identityStatus = identityStatus?.toDomain() ?: IdentityVerificationStatus(),
    verificationStatus = ProfileVerificationStatus.entries
        .firstOrNull { it.name == verificationStatus } ?: ProfileVerificationStatus.INCOMPLETE,
    statusMessage = statusMessage
)

private fun PersonalInfoDto.toDomain() = PersonalInfo(
    fullName = fullName.orEmpty(),
    gender = gender.orEmpty(),
    dateOfBirth = dateOfBirth.orEmpty(),
    phoneNumber = phoneNumber.orEmpty(),
    nationalId = nationalId.orEmpty(),
    issueDate = issueDate.orEmpty()
)

private fun AddressInfoDto.toDomain() = AddressInfo(
    permanentAddress = permanentAddress.orEmpty(),
    currentAddress = currentAddress.orEmpty()
)

private fun JobInfoDto.toDomain() = JobInfo(
    jobTitle = jobTitle.orEmpty(),
    monthlyIncome = monthlyIncome ?: 0L,
    companyName = companyName.orEmpty(),
    position = position.orEmpty()
)

private fun ContactInfoDto.toDomain() = ContactInfo(
    contactName = contactName.orEmpty(),
    relationship = relationship.orEmpty(),
    phoneNumber = phoneNumber.orEmpty()
)

private fun IdentityStatusDto.toDomain() = IdentityVerificationStatus(
    isFaceVerified = isFaceVerified ?: false,
    isNfcVerified = isNfcVerified ?: false,
    isDocumentUploadVerified = isDocumentUploadVerified ?: false,
    isBiometricEnabled = isBiometricEnabled ?: false
)

// ---- Mapping domain -> DTO (cho update) ----

fun UserProfile.toDto(): UserProfileDto = UserProfileDto(
    avatarUri = avatarUri,
    personalInfo = PersonalInfoDto(
        fullName = personalInfo.fullName,
        gender = personalInfo.gender,
        dateOfBirth = personalInfo.dateOfBirth,
        phoneNumber = personalInfo.phoneNumber,
        nationalId = personalInfo.nationalId,
        issueDate = personalInfo.issueDate
    ),
    addressInfo = AddressInfoDto(
        permanentAddress = addressInfo.permanentAddress,
        currentAddress = addressInfo.currentAddress
    ),
    jobInfo = JobInfoDto(
        jobTitle = jobInfo.jobTitle,
        monthlyIncome = jobInfo.monthlyIncome,
        companyName = jobInfo.companyName,
        position = jobInfo.position
    ),
    contactInfo = ContactInfoDto(
        contactName = contactInfo.contactName,
        relationship = contactInfo.relationship,
        phoneNumber = contactInfo.phoneNumber
    ),
    education = education,
    maritalStatus = maritalStatus,
    identityStatus = IdentityStatusDto(
        isFaceVerified = identityStatus.isFaceVerified,
        isNfcVerified = identityStatus.isNfcVerified,
        isDocumentUploadVerified = identityStatus.isDocumentUploadVerified,
        isBiometricEnabled = identityStatus.isBiometricEnabled
    ),
    verificationStatus = verificationStatus.name,
    statusMessage = statusMessage
)
