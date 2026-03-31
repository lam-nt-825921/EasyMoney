package com.example.easymoney.data.remote.mock

import com.example.easymoney.domain.model.LoanPackageModel

class LoanMockDataSource {
    private val mockLoanPackages = listOf(
        LoanPackageModel(
            id = "1",
            packageName = "Vay Nhanh",
            tenorRange = "6,12,18,24",
            minAmount = 6_000_000L,
            maxAmount = 100_000_000L,
            interest = 12.0,
            overdueCost = 5.0,
            eligibleCreditScore = 600
        ),
        LoanPackageModel(
            id = "2",
            packageName = "Vay Linh Hoat",
            tenorRange = "3,6,9,12",
            minAmount = 3_000_000L,
            maxAmount = 50_000_000L,
            interest = 10.5,
            overdueCost = 4.0,
            eligibleCreditScore = 550
        )
    )

    fun getLoanPackages(): List<LoanPackageModel> = mockLoanPackages

    fun getLoanPackageById(id: String): LoanPackageModel? {
        return mockLoanPackages.firstOrNull { it.id == id }
    }
}

