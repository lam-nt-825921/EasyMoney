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
    APPROVED, CANCELLED, SIGNED
}
