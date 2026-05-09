package com.example.easymoney.domain.model

enum class ProfileVerificationStatus {
    INCOMPLETE,
    PENDING,
    VERIFIED,
    EXPIRED,
    REJECTED
}

data class UserProfile(
    val personalInfo: PersonalInfo = PersonalInfo(),
    val addressInfo: AddressInfo = AddressInfo(),
    val jobInfo: JobInfo = JobInfo(),
    val contactInfo: ContactInfo = ContactInfo(),
    val education: String = "",
    val maritalStatus: String = "",
    val identityStatus: IdentityVerificationStatus = IdentityVerificationStatus(),
    val verificationStatus: ProfileVerificationStatus = ProfileVerificationStatus.INCOMPLETE,
    val statusMessage: String? = null
)

data class PersonalInfo(
    val fullName: String = "",
    val gender: String = "",
    val dateOfBirth: String = "",
    val phoneNumber: String = "",
    val nationalId: String = "",
    val issueDate: String = ""
)

data class AddressInfo(
    val permanentAddress: String = "",
    val currentAddress: String = ""
)

data class JobInfo(
    val jobTitle: String = "",
    val monthlyIncome: Long = 0L,
    val companyName: String = "",
    val position: String = ""
)

data class ContactInfo(
    val contactName: String = "",
    val relationship: String = "",
    val phoneNumber: String = ""
)

data class IdentityVerificationStatus(
    val isFaceVerified: Boolean = false,
    val isNfcVerified: Boolean = false,
    val isBiometricEnabled: Boolean = false
)

data class EKycStatus(
    val isIdentified: Boolean,
    val missingDocuments: List<String> = emptyList(),
    val message: String? = null
)
