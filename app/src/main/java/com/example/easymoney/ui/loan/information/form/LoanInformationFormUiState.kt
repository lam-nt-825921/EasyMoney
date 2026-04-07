package com.example.easymoney.ui.loan.information.form

import com.example.easymoney.domain.model.MasterDataItem

/**
 * Các loại Bottom Sheet cần hiển thị
 */
enum class FormSheetType {
    NONE,
    PROVINCE,
    DISTRICT,
    WARD,
    PROFESSION,
    POSITION,
    EDUCATION,
    MARITAL_STATUS,
    RELATIONSHIP
}

data class LoanInformationFormUiState(
    // Permanent Address
    val permanentProvince: MasterDataItem? = null,
    val permanentDistrict: MasterDataItem? = null,
    val permanentWard: MasterDataItem? = null,
    val permanentDetail: String = "",
    val hasPermanentAddress: Boolean = false,

    // Current Address
    val isCurrentSameAsPermanent: Boolean = true,
    val currentProvince: MasterDataItem? = null,
    val currentDistrict: MasterDataItem? = null,
    val currentWard: MasterDataItem? = null,
    val currentDetail: String = "",

    // Personal Info
    val monthlyIncome: String = "",
    val profession: MasterDataItem? = null,
    val position: MasterDataItem? = null,
    val education: MasterDataItem? = null,
    val maritalStatus: MasterDataItem? = null,

    // Emergency Contact
    val contactName: String = "",
    val contactRelationship: MasterDataItem? = null,
    val contactPhone: String = "",

    // Master Data Lists
    val provinces: List<MasterDataItem> = emptyList(),
    val districts: List<MasterDataItem> = emptyList(),
    val wards: List<MasterDataItem> = emptyList(),
    val professions: List<MasterDataItem> = emptyList(),
    val positions: List<MasterDataItem> = emptyList(),
    val educationLevels: List<MasterDataItem> = emptyList(),
    val maritalStatuses: List<MasterDataItem> = emptyList(),
    val relationships: List<MasterDataItem> = emptyList(),

    // UI State
    val activeSheet: FormSheetType = FormSheetType.NONE,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val validationError: String? = null,
    
    // Flag để xác định đang chọn địa chỉ cho mục nào
    val isSelectingPermanentAddress: Boolean = true
) {
    val isFormValid: Boolean
        get() {
            val isCurrentAddressValid = isCurrentSameAsPermanent || (
                currentProvince != null && currentDistrict != null && currentWard != null && currentDetail.isNotBlank()
            )
            
            return permanentProvince != null && permanentDistrict != null && permanentWard != null && permanentDetail.isNotBlank() &&
                   isCurrentAddressValid &&
                   monthlyIncome.isNotBlank() && profession != null && position != null && education != null && maritalStatus != null &&
                   contactName.isNotBlank() && contactRelationship != null && contactPhone.length >= 10
        }
}
