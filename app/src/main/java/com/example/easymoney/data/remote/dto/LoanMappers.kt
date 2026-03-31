package com.example.easymoney.data.remote.dto

import com.example.easymoney.domain.model.LoanPackageModel

fun LoanPackageDto.toDomain(): LoanPackageModel {
    return LoanPackageModel(
        id = id.orEmpty(),
        packageName = packageName.orEmpty(),
        tenorRange = tenorRange ?: "6,12,18,24",
        minAmount = minAmount ?: 0L,
        maxAmount = maxAmount ?: 0L,
        interest = interest ?: 0.0,
        overdueCost = overdueCost ?: 0.0,
        eligibleCreditScore = eligibleCreditScore ?: 0
    )
}

