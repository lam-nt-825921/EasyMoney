package com.example.easymoney.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoanPackageDto(
    @SerializedName(value = "id", alternate = ["package_id"])
    val id: String? = null,
    @SerializedName(value = "package_name", alternate = ["name"])
    val packageName: String? = null,
    @SerializedName(value = "tenor_range", alternate = ["tenor"])
    val tenorRange: String? = null,
    @SerializedName(value = "min_amount", alternate = ["minimum_amount"])
    val minAmount: Long? = null,
    @SerializedName(value = "max_amount", alternate = ["maximum_amount"])
    val maxAmount: Long? = null,
    @SerializedName(value = "interest", alternate = ["interest_rate"])
    val interest: Double? = null,
    @SerializedName(value = "overdue_cost", alternate = ["late_fee"])
    val overdueCost: Double? = null,
    @SerializedName(value = "eligible_credit_score", alternate = ["credit_score"])
    val eligibleCreditScore: Int? = null
)

