package com.example.easymoney.domain.model

enum class ProfileVerificationStatus {
    INCOMPLETE,
    PENDING,
    VERIFIED,
    EXPIRED,
    REJECTED
}

data class UserProfile(
    val avatarUri: String = "",
    val personalInfo: PersonalInfo = PersonalInfo(),
    val addressInfo: AddressInfo = AddressInfo(),
    val jobInfo: JobInfo = JobInfo(),
    val contactInfo: ContactInfo = ContactInfo(),
    val education: String = "",
    val maritalStatus: String = "",
    val identityStatus: IdentityVerificationStatus = IdentityVerificationStatus(),
    val verificationStatus: ProfileVerificationStatus = ProfileVerificationStatus.INCOMPLETE,
    val statusMessage: String? = null,
    val isProfileCompleted: Boolean = false,
    val missingFields: List<String> = emptyList()
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
    // Workflow #35 — document upload là phương thức thay thế cho NFC để xác thực giấy tờ.
    val isDocumentUploadVerified: Boolean = false,
    val isBiometricEnabled: Boolean = false
) {
    /**
     * Workflow #35 — Phần định danh giấy tờ tính là hoàn thiện nếu một trong hai
     * (NFC hoặc upload giấy tờ) đã pass. Biometric là 2FA optional, không tính.
     */
    val isIdentityDocumentVerified: Boolean
        get() = isNfcVerified || isDocumentUploadVerified
}

data class EKycStatus(
    val isIdentified: Boolean,
    val missingDocuments: List<String> = emptyList(),
    val message: String? = null,
    // Workflow #59 — optional rich fields từ backend; UI hiện tại bỏ qua, future-friendly.
    val status: String? = null,
    val sessionId: String? = null,
    val documentMethod: String? = null,
    val verifiedAt: Long? = null,
    val matchScore: Double? = null
)

data class ProfileCompletion(
    val isProfileCompleted: Boolean,
    val canApplyLoan: Boolean,
    val completionPercent: Int,
    val missingFields: List<String>,
    val nextAction: String,
    val statusMessage: String
)
