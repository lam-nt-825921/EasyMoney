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

    // UI State for BottomSheets
    val activeSheet: FormSheetType = FormSheetType.NONE
)

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
                    _uiState.update { it.copy(profile = result.data, isLoading = false) }
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
            state.copy(profile = updatedProfile, activeSheet = FormSheetType.NONE)
        }
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
            )
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
            )
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
            )
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

    fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = userRepository.updateProfile(_uiState.value.profile)
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    isSuccess = result is Resource.Success,
                    errorMessage = if (result is Resource.Error) result.message else null
                ) 
            }
        }
    }
}
