package com.example.easymoney.ui.loan.information.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.MasterDataItem
import com.example.easymoney.domain.repository.LoanRepository
import com.example.easymoney.domain.repository.UserRepository
import com.example.easymoney.utils.UiText
import com.example.easymoney.utils.currentAppLanguage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanInformationFormViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoanInformationFormUiState())
    val uiState: StateFlow<LoanInformationFormUiState> = _uiState.asStateFlow()

    init {
        loadMyInfo()
        loadMasterData()
        prefillFromProfile()
    }

    private fun loadMyInfo() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = loanRepository.getMyInfo()) {
                is Resource.Success -> {
                    val info = result.data
                    val hasPermanent = info.permanentProvince != null
                    _uiState.update {
                        it.copy(
                            permanentProvince = info.permanentProvince?.let { p -> MasterDataItem(p, p) },
                            permanentDistrict = info.permanentDistrict?.let { d -> MasterDataItem(d, d) },
                            permanentWard = info.permanentWard?.let { w -> MasterDataItem(w, w) },
                            permanentDetail = info.permanentDetail ?: "",
                            hasPermanentAddress = hasPermanent,
                            isCurrentSameAsPermanent = hasPermanent,
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    val errorMessage = result.message
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = UiText.DynamicString(errorMessage))
                    }
                }
                else -> { _uiState.update { it.copy(isLoading = false) } }
            }
        }
    }

    private fun loadMasterData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = loanRepository.getMasterDataMetadata(currentAppLanguage())) {
                is Resource.Success -> {
                    val metadata = result.data
                    _uiState.update {
                        it.copy(
                            provinces = metadata.provinces,
                            professions = metadata.professions,
                            positions = metadata.positions,
                            educationLevels = metadata.educationLevels,
                            maritalStatuses = metadata.maritalStatuses,
                            relationships = metadata.relationships,
                            isLoading = false
                        )
                    }
                }
                is Resource.Error -> {
                    val errorMessage = result.message
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = UiText.DynamicString(errorMessage))
                    }
                }
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun prefillFromProfile() {
        viewModelScope.launch {
            when (val result = userRepository.getProfile()) {
                is Resource.Success -> {
                    val profile = result.data
                    val permanentAddress = parseAddress(profile.addressInfo.permanentAddress)
                    val currentAddress = parseAddress(profile.addressInfo.currentAddress)

                    _uiState.update { state ->
                        val hasPermanentAddress = state.hasPermanentAddress || permanentAddress.hasAnyValue()
                        state.copy(
                            permanentProvince = state.permanentProvince ?: permanentAddress.province,
                            permanentDistrict = state.permanentDistrict ?: permanentAddress.district,
                            permanentWard = state.permanentWard ?: permanentAddress.ward,
                            permanentDetail = state.permanentDetail.ifBlank { permanentAddress.detail },
                            hasPermanentAddress = hasPermanentAddress,
                            isCurrentSameAsPermanent = if (currentAddress.hasAnyValue()) false else state.isCurrentSameAsPermanent,
                            currentProvince = state.currentProvince ?: currentAddress.province,
                            currentDistrict = state.currentDistrict ?: currentAddress.district,
                            currentWard = state.currentWard ?: currentAddress.ward,
                            currentDetail = state.currentDetail.ifBlank { currentAddress.detail },
                            monthlyIncome = state.monthlyIncome.ifBlank {
                                profile.jobInfo.monthlyIncome.takeIf { it > 0L }?.toString().orEmpty()
                            },
                            profession = state.profession ?: profile.jobInfo.jobTitle.toCachedItem(),
                            position = state.position ?: profile.jobInfo.position.toCachedItem(),
                            companyName = state.companyName.ifBlank { profile.jobInfo.companyName },
                            education = state.education ?: profile.education.toCachedItem(),
                            maritalStatus = state.maritalStatus ?: profile.maritalStatus.toCachedItem(),
                            contactName = state.contactName.ifBlank { profile.contactInfo.contactName },
                            contactRelationship = state.contactRelationship ?: profile.contactInfo.relationship.toCachedItem(),
                            contactPhone = state.contactPhone.ifBlank { profile.contactInfo.phoneNumber }
                        )
                    }
                }
                else -> Unit
            }
        }
    }

    fun onShowSheet(sheetType: FormSheetType, isPermanent: Boolean = false) {
        _uiState.update { it.copy(activeSheet = sheetType, isSelectingPermanentAddress = isPermanent) }
        if (sheetType == FormSheetType.DISTRICT) {
            val provinceId = if (isPermanent) _uiState.value.permanentProvince?.id else _uiState.value.currentProvince?.id
            provinceId?.let { loadDistricts(it) }
        } else if (sheetType == FormSheetType.WARD) {
            val districtId = if (isPermanent) _uiState.value.permanentDistrict?.id else _uiState.value.currentDistrict?.id
            districtId?.let { loadWards(it) }
        }
    }

    private fun loadDistricts(provinceId: String) {
        viewModelScope.launch {
            val result = loanRepository.getDistricts(provinceId, currentAppLanguage())
            if (result is Resource.Success) _uiState.update { it.copy(districts = result.data) }
        }
    }

    private fun loadWards(districtId: String) {
        viewModelScope.launch {
            val result = loanRepository.getWards(districtId, currentAppLanguage())
            if (result is Resource.Success) _uiState.update { it.copy(wards = result.data) }
        }
    }

    fun triggerValidation(): Boolean {
        val errors = _uiState.value.validateForm()
        _uiState.update { it.copy(fieldErrors = errors, showErrors = true) }
        return errors.isEmpty()
    }

    fun onSelectItem(item: MasterDataItem) {
        val sheetType = _uiState.value.activeSheet
        val isPermanent = _uiState.value.isSelectingPermanentAddress
        _uiState.update { state ->
            when (sheetType) {
                FormSheetType.PROVINCE -> {
                    if (isPermanent) state.copy(permanentProvince = item, permanentDistrict = null, permanentWard = null, activeSheet = FormSheetType.DISTRICT)
                    else state.copy(currentProvince = item, currentDistrict = null, currentWard = null, activeSheet = FormSheetType.DISTRICT)
                }
                FormSheetType.DISTRICT -> {
                    if (isPermanent) state.copy(permanentDistrict = item, permanentWard = null, activeSheet = FormSheetType.WARD)
                    else state.copy(currentDistrict = item, currentWard = null, activeSheet = FormSheetType.WARD)
                }
                FormSheetType.WARD -> {
                    if (isPermanent) state.copy(permanentWard = item, activeSheet = FormSheetType.NONE)
                    else state.copy(currentWard = item, activeSheet = FormSheetType.NONE)
                }
                FormSheetType.PROFESSION -> state.copy(profession = item, activeSheet = FormSheetType.NONE)
                FormSheetType.POSITION -> state.copy(position = item, activeSheet = FormSheetType.NONE)
                FormSheetType.EDUCATION -> state.copy(education = item, activeSheet = FormSheetType.NONE)
                FormSheetType.MARITAL_STATUS -> state.copy(maritalStatus = item, activeSheet = FormSheetType.NONE)
                FormSheetType.RELATIONSHIP -> state.copy(contactRelationship = item, activeSheet = FormSheetType.NONE)
                else -> state.copy(activeSheet = FormSheetType.NONE)
            }
        }
        val currentState = _uiState.value
        when (currentState.activeSheet) {
            FormSheetType.DISTRICT -> loadDistricts(item.id)
            FormSheetType.WARD -> loadWards(item.id)
            else -> {}
        }
    }

    fun onBackSheet() {
        val sheetType = _uiState.value.activeSheet
        _uiState.update { state ->
            when (sheetType) {
                FormSheetType.DISTRICT -> state.copy(activeSheet = FormSheetType.PROVINCE)
                FormSheetType.WARD -> state.copy(activeSheet = FormSheetType.DISTRICT)
                else -> state.copy(activeSheet = FormSheetType.NONE)
            }
        }
    }

    fun onDismissSheet() {
        _uiState.update { it.copy(activeSheet = FormSheetType.NONE) }
    }

    fun onCurrentAddressToggle(isSame: Boolean) {
        if (!_uiState.value.hasPermanentAddress && isSame) return
        _uiState.update { it.copy(isCurrentSameAsPermanent = isSame) }
    }

    fun onDetailAddressChanged(value: String, isPermanent: Boolean) {
        if (isPermanent) _uiState.update { it.copy(permanentDetail = value) }
        else _uiState.update { it.copy(currentDetail = value) }
    }

    fun onMonthlyIncomeChanged(value: String) {
        // CHỈ LƯU SỐ THUẦN TÚY (No formatting here to avoid cursor jumping)
        val cleanValue = value.filter { it.isDigit() }
        _uiState.update { it.copy(monthlyIncome = cleanValue) }
    }

    fun onCompanyNameChanged(value: String) {
        _uiState.update { it.copy(companyName = value) }
    }

    fun onSpouseNameChanged(value: String) {
        _uiState.update { it.copy(spouseName = value) }
    }

    fun onSpousePhoneChanged(value: String) {
        _uiState.update { it.copy(spousePhone = value) }
    }

    fun onContactNameChanged(value: String) {
        _uiState.update { it.copy(contactName = value) }
    }

    fun onContactPhoneChanged(value: String) {
        _uiState.update { it.copy(contactPhone = value) }
    }

    private fun parseAddress(rawAddress: String): CachedAddress {
        val parts = rawAddress
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        if (parts.isEmpty()) return CachedAddress()

        val province = parts.getOrNull(parts.lastIndex).toCachedItem()
        val district = parts.getOrNull(parts.lastIndex - 1).toCachedItem()
        val ward = parts.getOrNull(parts.lastIndex - 2).toCachedItem()
        val detail = if (parts.size >= 4) {
            parts.dropLast(3).joinToString(", ")
        } else {
            parts.first()
        }

        return CachedAddress(
            province = province,
            district = district,
            ward = ward,
            detail = detail
        )
    }

    private fun String?.toCachedItem(): MasterDataItem? {
        val value = this?.trim().orEmpty()
        return value.takeIf { it.isNotBlank() }?.let { MasterDataItem(id = it, name = it) }
    }

    private data class CachedAddress(
        val province: MasterDataItem? = null,
        val district: MasterDataItem? = null,
        val ward: MasterDataItem? = null,
        val detail: String = ""
    ) {
        fun hasAnyValue(): Boolean = province != null || district != null || ward != null || detail.isNotBlank()
    }
}
