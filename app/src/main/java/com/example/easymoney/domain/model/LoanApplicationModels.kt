package com.example.easymoney.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Request gửi lên server để lưu thông tin hồ sơ vay
 */
data class LoanApplicationRequest(
    @SerializedName("packageId")
    val packageId: String? = null,
    @SerializedName("loanAmount")
    val loanAmount: Long,
    @SerializedName("tenorMonth")
    val tenorMonth: Int,
    @SerializedName("hasInsurance")
    val hasInsurance: Boolean,
    @SerializedName("ekycMatchKey")
    val ekycMatchKey: String? = null,
    @SerializedName("voucherId")
    val voucherId: String? = null,
    
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

data class ApplicableVoucher(
    val id: String,
    val rewardId: String,
    val title: String,
    val benefitType: String,
    val benefitValue: Double? = null,
    val status: String,
    val discountAmount: Long = 0,
    val finalInterestRate: Double? = null,
    val expiresAt: Long? = null
)

data class LoanQuoteRequest(
    @SerializedName("loanAmount")
    val loanAmount: Long,
    @SerializedName("tenorMonth")
    val tenorMonth: Int,
    @SerializedName("hasInsurance")
    val hasInsurance: Boolean = false,
    @SerializedName("voucherId")
    val voucherId: String? = null
)

data class LoanQuote(
    val packageId: String,
    val loanAmount: Long,
    val tenorMonth: Int,
    val hasInsurance: Boolean,
    val originalInterestRate: Double,
    val finalInterestRate: Double,
    val monthlyPrincipal: Long,
    val monthlyInterest: Long,
    val monthlyPayment: Long,
    val totalInterest: Long,
    val insuranceFee: Long,
    val discountAmount: Long,
    val totalPayment: Long,
    val voucherId: String? = null,
    val voucherTitle: String? = null
)

data class LoanSubmitResponse(
    val applicationId: String,
    val status: String,
    val message: String,
    val quote: LoanQuote? = null
)

data class EkycMatchResponse(
    val isMatched: Boolean,
    val canApplyLoan: Boolean,
    val reasonCode: String? = null,
    val message: String,
    val missingSteps: List<String> = emptyList(),
    val mismatchedFields: List<String> = emptyList(),
    val documentMethod: String? = null,
    val faceVerifiedAt: String? = null,
    val documentVerifiedAt: String? = null,
    val packageId: String? = null,
    val ekycMatchKey: String? = null,
    val expiresAt: Long? = null
)

/**
 * Model cho các item chọn trong BottomSheet (Tỉnh thành, Nghề nghiệp, v.v.)
 */
data class MasterDataItem(
    val id: String,
    val name: String,
    val parentId: String? = null
)
