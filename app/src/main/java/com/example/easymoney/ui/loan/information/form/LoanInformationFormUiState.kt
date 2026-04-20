package com.example.easymoney.ui.loan.information.form

import com.example.easymoney.domain.model.MasterDataItem
import com.example.easymoney.utils.UiText
import com.example.easymoney.R

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
    
    // Conditional Info
    val companyName: String = "",
    val spouseName: String = "",
    val spousePhone: String = "",

    // Emergency Contact
    val contactName: String = "",
    val contactRelationship: MasterDataItem? = null,
    val contactPhone: String = "",
    
    // Payout Info (Mock)
    val bankName: String = "ViettelPay",
    val accountNumber: String = "0987555664646",
    val accountOwner: String = "Hoàng Trung Tuấn",

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
    val errorMessage: UiText? = null,
    val isSelectingPermanentAddress: Boolean = true,
    
    // Validation Errors (Key là tên trường, Value là nội dung lỗi)
    val fieldErrors: Map<String, UiText> = emptyMap(),
    val showErrors: Boolean = false
) {
    val isFormValid: Boolean
        get() = validateForm().isEmpty()

    fun validateForm(): Map<String, UiText> {
        val errors = mutableMapOf<String, UiText>()
        
        if (permanentProvince == null) errors["permanentProvince"] = UiText.StringResource(R.string.error_select_province)
        if (permanentDistrict == null) errors["permanentDistrict"] = UiText.StringResource(R.string.error_select_district)
        if (permanentWard == null) errors["permanentWard"] = UiText.StringResource(R.string.error_select_ward)
        if (permanentDetail.isBlank()) errors["permanentDetail"] = UiText.StringResource(R.string.error_input_address_detail)
        
        if (!isCurrentSameAsPermanent) {
            if (currentProvince == null) errors["currentProvince"] = UiText.StringResource(R.string.error_select_province)
            if (currentDistrict == null) errors["currentDistrict"] = UiText.StringResource(R.string.error_select_district)
            if (currentWard == null) errors["currentWard"] = UiText.StringResource(R.string.error_select_ward)
            if (currentDetail.isBlank()) errors["currentDetail"] = UiText.StringResource(R.string.error_input_address_detail)
        }
        
        if (monthlyIncome.isBlank()) errors["monthlyIncome"] = UiText.StringResource(R.string.error_input_income)
        if (profession == null) errors["profession"] = UiText.StringResource(R.string.error_select_profession)
        if (education == null) errors["education"] = UiText.StringResource(R.string.error_select_education)
        if (maritalStatus == null) errors["maritalStatus"] = UiText.StringResource(R.string.error_select_marital_status)
        
        if (profession?.id == "p1") {
            if (companyName.isBlank()) errors["companyName"] = UiText.StringResource(R.string.error_input_company_name)
            if (position == null) errors["position"] = UiText.StringResource(R.string.error_select_position)
        }
        
        if (maritalStatus?.id == "m2") {
            if (spouseName.isBlank()) errors["spouseName"] = UiText.StringResource(R.string.error_input_spouse_name)
            if (spousePhone.length < 10) errors["spousePhone"] = UiText.StringResource(R.string.error_invalid_phone)
        }
        
        if (contactName.isBlank()) errors["contactName"] = UiText.StringResource(R.string.error_input_contact_name)
        if (contactRelationship == null) errors["contactRelationship"] = UiText.StringResource(R.string.error_select_relationship)
        if (contactPhone.length < 10) errors["contactPhone"] = UiText.StringResource(R.string.error_invalid_phone)
        
        return errors
    }
}
