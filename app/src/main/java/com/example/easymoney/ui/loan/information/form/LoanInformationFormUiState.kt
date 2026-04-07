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
    val errorMessage: String? = null,
    val isSelectingPermanentAddress: Boolean = true,
    
    // Validation Errors (Key là tên trường, Value là nội dung lỗi)
    val fieldErrors: Map<String, String> = emptyMap(),
    val showErrors: Boolean = false
) {
    val isFormValid: Boolean
        get() = validateForm().isEmpty()

    fun validateForm(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        if (permanentProvince == null) errors["permanentProvince"] = "Vui lòng chọn Tỉnh/Thành phố"
        if (permanentDistrict == null) errors["permanentDistrict"] = "Vui lòng chọn Quận/Huyện"
        if (permanentWard == null) errors["permanentWard"] = "Vui lòng chọn Phường/Xã"
        if (permanentDetail.isBlank()) errors["permanentDetail"] = "Vui lòng nhập địa chỉ chi tiết"
        
        if (!isCurrentSameAsPermanent) {
            if (currentProvince == null) errors["currentProvince"] = "Vui lòng chọn Tỉnh/Thành phố"
            if (currentDistrict == null) errors["currentDistrict"] = "Vui lòng chọn Quận/Huyện"
            if (currentWard == null) errors["currentWard"] = "Vui lòng chọn Phường/Xã"
            if (currentDetail.isBlank()) errors["currentDetail"] = "Vui lòng nhập địa chỉ chi tiết"
        }
        
        if (monthlyIncome.isBlank()) errors["monthlyIncome"] = "Vui lòng nhập thu nhập"
        if (profession == null) errors["profession"] = "Vui lòng chọn nghề nghiệp"
        if (education == null) errors["education"] = "Vui lòng chọn trình độ"
        if (maritalStatus == null) errors["maritalStatus"] = "Vui lòng chọn tình trạng hôn nhân"
        
        if (profession?.id == "p1") {
            if (companyName.isBlank()) errors["companyName"] = "Vui lòng nhập tên công ty"
            if (position == null) errors["position"] = "Vui lòng chọn chức vụ"
        }
        
        if (maritalStatus?.id == "m2") {
            if (spouseName.isBlank()) errors["spouseName"] = "Vui lòng nhập tên vợ/chồng"
            if (spousePhone.length < 10) errors["spousePhone"] = "Số điện thoại không hợp lệ"
        }
        
        if (contactName.isBlank()) errors["contactName"] = "Vui lòng nhập tên người liên hệ"
        if (contactRelationship == null) errors["contactRelationship"] = "Vui lòng chọn mối quan hệ"
        if (contactPhone.length < 10) errors["contactPhone"] = "Số điện thoại không hợp lệ"
        
        return errors
    }
}
