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
    @SerializedName("permanentProvince")
    val permanentProvince: String,
    @SerializedName("permanentDistrict")
    val permanentDistrict: String,
    @SerializedName("permanentWard")
    val permanentWard: String,
    @SerializedName("permanentDetail")
    val permanentDetail: String,

    @SerializedName("currentProvince")
    val currentProvince: String,
    @SerializedName("currentDistrict")
    val currentDistrict: String,
    @SerializedName("currentWard")
    val currentWard: String,
    @SerializedName("currentDetail")
    val currentDetail: String,

    // Personal Info
    @SerializedName("monthlyIncome")
    val monthlyIncome: Long,
    @SerializedName("profession")
    val profession: String,
    @SerializedName("position")
    val position: String,
    @SerializedName("education")
    val education: String,
    @SerializedName("maritalStatus")
    val maritalStatus: String,

    // Emergency Contact
    @SerializedName("contactName")
    val contactName: String,
    @SerializedName("contactRelationship")
    val contactRelationship: String,
    @SerializedName("contactPhone")
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
