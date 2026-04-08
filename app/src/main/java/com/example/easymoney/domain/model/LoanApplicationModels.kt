package com.example.easymoney.domain.model

/**
 * Request gửi lên server để lưu thông tin hồ sơ vay
 */
data class LoanApplicationRequest(
    val loanAmount: Long,
    val tenorMonth: Int,
    val hasInsurance: Boolean,
    
    // Address
    val permanentProvince: String,
    val permanentDistrict: String,
    val permanentWard: String,
    val permanentDetail: String,
    
    val currentProvince: String,
    val currentDistrict: String,
    val currentWard: String,
    val currentDetail: String,
    
    // Personal Info
    val monthlyIncome: Long,
    val profession: String,
    val position: String,
    val education: String,
    val maritalStatus: String,
    
    // Emergency Contact
    val contactName: String,
    val contactRelationship: String,
    val contactPhone: String
)

/**
 * Model cho các item chọn trong BottomSheet (Tỉnh thành, Nghề nghiệp, v.v.)
 */
data class MasterDataItem(
    val id: String,
    val name: String,
    val parentId: String? = null
)
