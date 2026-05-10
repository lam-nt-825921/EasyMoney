package com.example.easymoney.data.sample

import com.example.easymoney.domain.model.ContractStatus
import com.example.easymoney.domain.model.LoanContractModel

val SAMPLE_APPROVED_CONTRACTS: List<LoanContractModel> = listOf(
    LoanContractModel(
        id = "C001",
        contractNumber = "HD-2026-001",
        amount = 30_000_000L,
        termMonths = 12,
        interestRate = 1.2,
        approvedAt = System.currentTimeMillis() - 86400000L,
        status = ContractStatus.APPROVED
    ),
    LoanContractModel(
        id = "C002",
        contractNumber = "HD-2026-002",
        amount = 50_000_000L,
        termMonths = 18,
        interestRate = 1.4,
        approvedAt = System.currentTimeMillis() - 86400000L * 2,
        status = ContractStatus.APPROVED
    )
)
