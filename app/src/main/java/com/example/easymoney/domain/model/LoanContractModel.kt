package com.example.easymoney.domain.model

/** Hợp đồng vay đã được hệ thống duyệt — workflow #12. */
data class LoanContractModel(
    val id: String,
    val contractNumber: String,
    val amount: Long,
    val termMonths: Int,
    val interestRate: Double,
    val approvedAt: Long,
    val status: ContractStatus
)

enum class ContractStatus {
    APPROVED, CANCELLED, SIGNED, DISBURSED
}

data class LoanDebtModel(
    val id: Long,
    val applicationId: String,
    val totalAmount: Long,
    val remainingPrincipal: Long,
    val monthlyPayment: Long,
    val interestRate: Double,
    val totalMonths: Int,
    val monthsPaid: Int,
    val status: String,
    val createdAt: String
)

enum class RepayType(val apiValue: String) {
    MONTHLY("MONTHLY"),
    FULL_EARLY("FULL_EARLY")
}
