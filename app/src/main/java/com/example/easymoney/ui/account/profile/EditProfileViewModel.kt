package com.example.easymoney.ui.account.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.*
import com.example.easymoney.domain.repository.UserRepository
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.ui.loan.information.form.FormSheetType
import com.example.easymoney.utils.currentAppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val profile: UserProfile = UserProfile(),
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,

    // Master Data
    val professions: List<MasterDataItem> = emptyList(),
    val positions: List<MasterDataItem> = emptyList(),
    val educationLevels: List<MasterDataItem> = emptyList(),
    val maritalStatuses: List<MasterDataItem> = emptyList(),
    val relationships: List<MasterDataItem> = emptyList(),

    // Selected Items (to track IDs)
    val selectedProfession: MasterDataItem? = null,
    val selectedPosition: MasterDataItem? = null,
    val selectedEducation: MasterDataItem? = null,
    val selectedMaritalStatus: MasterDataItem? = null,
    val selectedRelationship: MasterDataItem? = null,

    // UI State for BottomSheets
    val activeSheet: FormSheetType = FormSheetType.NONE,

    // Workflow #95 — lỗi validate theo từng trường
    val fieldErrors: Map<ProfileField, ProfileValidationError> = emptyMap()
) {
    /** Màn thông tin cá nhân hợp lệ khi không trường nào lỗi. */
    val isPersonalInfoValid: Boolean
        get() = PERSONAL_FIELDS.none { fieldErrors.containsKey(it) }

    /** Màn người liên hệ hợp lệ khi tên + SĐT hợp lệ và đã chọn mối quan hệ. */
    val isContactInfoValid: Boolean
        get() = CONTACT_FIELDS.none { fieldErrors.containsKey(it) } &&
            profile.contactInfo.relationship.isNotBlank()

    private companion object {
        val PERSONAL_FIELDS = listOf(
            ProfileField.FULL_NAME,
            ProfileField.NATIONAL_ID,
            ProfileField.GENDER,
            ProfileField.DATE_OF_BIRTH
        )
        val CONTACT_FIELDS = listOf(ProfileField.CONTACT_NAME, ProfileField.CONTACT_PHONE)
    }
}

enum class EditProfileSection {
    PERSONAL,
    CONTACT,
    JOB
}

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val loanRepository: LoanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        loadMasterData()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = userRepository.getProfile()) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(profile = result.data, isLoading = false)
                            .withRecomputedErrors()
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(errorMessage = result.message, isLoading = false) }
                }
                else -> {}
            }
        }
    }

    private fun loadMasterData() {
        viewModelScope.launch {
            val lang = currentAppLanguage()
            val professions = loanRepository.getProfessions(lang)
            val positions = loanRepository.getPositions(lang)
            val education = loanRepository.getEducationLevels(lang)
            val marital = loanRepository.getMaritalStatuses(lang)
            val relationships = loanRepository.getRelationships(lang)

            _uiState.update { state ->
                state.copy(
                    professions = if (professions is Resource.Success) professions.data else emptyList(),
                    positions = if (positions is Resource.Success) positions.data else emptyList(),
                    educationLevels = if (education is Resource.Success) education.data else emptyList(),
                    maritalStatuses = if (marital is Resource.Success) marital.data else emptyList(),
                    relationships = if (relationships is Resource.Success) relationships.data else emptyList()
                )
            }
        }
    }

    fun onShowSheet(type: FormSheetType) {
        _uiState.update { it.copy(activeSheet = type) }
    }

    fun onDismissSheet() {
        _uiState.update { it.copy(activeSheet = FormSheetType.NONE) }
    }

    fun onSelectItem(item: MasterDataItem) {
        val currentSheet = _uiState.value.activeSheet
        _uiState.update { state ->
            val updatedProfile = when (currentSheet) {
                FormSheetType.PROFESSION -> state.profile.copy(jobInfo = state.profile.jobInfo.copy(jobTitle = item.name))
                FormSheetType.POSITION -> state.profile.copy(jobInfo = state.profile.jobInfo.copy(position = item.name))
                FormSheetType.EDUCATION -> state.profile.copy(education = item.name)
                FormSheetType.MARITAL_STATUS -> state.profile.copy(maritalStatus = item.name)
                FormSheetType.RELATIONSHIP -> state.profile.copy(contactInfo = state.profile.contactInfo.copy(relationship = item.name))
                else -> state.profile
            }
            
            val updatedState = when (currentSheet) {
                FormSheetType.PROFESSION -> state.copy(selectedProfession = item)
                FormSheetType.POSITION -> state.copy(selectedPosition = item)
                FormSheetType.EDUCATION -> state.copy(selectedEducation = item)
                FormSheetType.MARITAL_STATUS -> state.copy(selectedMaritalStatus = item)
                FormSheetType.RELATIONSHIP -> state.copy(selectedRelationship = item)
                else -> state
            }
            
            updatedState.copy(profile = updatedProfile, activeSheet = FormSheetType.NONE)
                .withRecomputedErrors()
        }
    }

    /** Workflow #95 — chọn giới tính từ bottom sheet cố định (Nam/Nữ). */
    fun onSelectGender(gender: String) {
        updatePersonalInfo(gender = gender)
        onDismissSheet()
    }

    fun updatePersonalInfo(
        fullName: String? = null,
        gender: String? = null,
        dob: String? = null,
        nationalId: String? = null
    ) {
        _uiState.update { state ->
            val current = state.profile.personalInfo
            state.copy(
                profile = state.profile.copy(
                    personalInfo = current.copy(
                        fullName = fullName ?: current.fullName,
                        gender = gender ?: current.gender,
                        dateOfBirth = dob ?: current.dateOfBirth,
                        nationalId = nationalId ?: current.nationalId
                    )
                )
            ).withRecomputedErrors()
        }
    }

    fun updateJobInfo(
        jobTitle: String? = null,
        income: Long? = null,
        company: String? = null,
        position: String? = null
    ) {
        _uiState.update { state ->
            val current = state.profile.jobInfo
            state.copy(
                profile = state.profile.copy(
                    jobInfo = current.copy(
                        jobTitle = jobTitle ?: current.jobTitle,
                        monthlyIncome = income ?: current.monthlyIncome,
                        companyName = company ?: current.companyName,
                        position = position ?: current.position
                    )
                )
            ).withRecomputedErrors()
        }
    }

    fun updateContactInfo(
        name: String? = null,
        relationship: String? = null,
        phone: String? = null
    ) {
        _uiState.update { state ->
            val current = state.profile.contactInfo
            state.copy(
                profile = state.profile.copy(
                    contactInfo = current.copy(
                        contactName = name ?: current.contactName,
                        relationship = relationship ?: current.relationship,
                        phoneNumber = phone ?: current.phoneNumber
                    )
                )
            ).withRecomputedErrors()
        }
    }

    fun updateAdditionalInfo(
        education: String? = null,
        maritalStatus: String? = null
    ) {
        _uiState.update { state ->
            state.copy(
                profile = state.profile.copy(
                    education = education ?: state.profile.education,
                    maritalStatus = maritalStatus ?: state.profile.maritalStatus
                )
            )
        }
    }

    fun saveProfile(section: EditProfileSection) {
        // Workflow #95 — chỉ chặn theo nhóm trường đang sửa, tránh lỗi ở màn khác làm save im lặng.
        val current = _uiState.value.withRecomputedErrors()
        val blockingErrors = current.fieldErrors.filterKeys { it in section.validatedFields }
        if (blockingErrors.isNotEmpty()) {
            _uiState.value = current
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = userRepository.updateProfile(normalizeForSave(current.profile))
            if (result is Resource.Success) {
                userRepository.getProfileCompletion(forceRefresh = true)
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isSuccess = result is Resource.Success,
                    errorMessage = if (result is Resource.Error) result.message else null
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /** Workflow #95 — chuẩn hoá giá trị nhập tay ngay trước khi gửi lên backend. */
    private fun normalizeForSave(profile: UserProfile): UserProfile = profile.copy(
        personalInfo = profile.personalInfo.copy(
            fullName = ProfileInputValidator.normalizeName(profile.personalInfo.fullName),
            nationalId = ProfileInputValidator.normalizeDigits(profile.personalInfo.nationalId)
        ),
        contactInfo = profile.contactInfo.copy(
            contactName = ProfileInputValidator.normalizeName(profile.contactInfo.contactName),
            phoneNumber = ProfileInputValidator.normalizePhone(profile.contactInfo.phoneNumber)
        )
    )

    /** Workflow #95 — tính lại lỗi validate cho toàn bộ trường nhập tay. */
    private fun EditProfileUiState.withRecomputedErrors(): EditProfileUiState =
        copy(fieldErrors = computeErrors(profile))

    private fun computeErrors(profile: UserProfile): Map<ProfileField, ProfileValidationError> {
        val personal = profile.personalInfo
        val contact = profile.contactInfo
        return buildMap {
            ProfileInputValidator.validateName(personal.fullName)
                ?.let { put(ProfileField.FULL_NAME, it) }
            ProfileInputValidator.validateNationalId(personal.nationalId)
                ?.let { put(ProfileField.NATIONAL_ID, it) }
            ProfileInputValidator.validateGender(personal.gender)
                ?.let { put(ProfileField.GENDER, it) }
            ProfileInputValidator.validateDateOfBirth(personal.dateOfBirth)
                ?.let { put(ProfileField.DATE_OF_BIRTH, it) }
            ProfileInputValidator.validateName(contact.contactName)
                ?.let { put(ProfileField.CONTACT_NAME, it) }
            ProfileInputValidator.validatePhone(contact.phoneNumber)
                ?.let { put(ProfileField.CONTACT_PHONE, it) }
        }
    }

    private val EditProfileSection.validatedFields: Set<ProfileField>
        get() = when (this) {
            EditProfileSection.PERSONAL -> setOf(
                ProfileField.FULL_NAME,
                ProfileField.NATIONAL_ID,
                ProfileField.GENDER,
                ProfileField.DATE_OF_BIRTH
            )
            EditProfileSection.CONTACT -> setOf(
                ProfileField.CONTACT_NAME,
                ProfileField.CONTACT_PHONE
            )
            EditProfileSection.JOB -> emptySet()
        }
}
