package com.example.easymoney.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoanPackagesResponseDto(
    @SerializedName(value = "items", alternate = ["data", "results"])
    val items: List<LoanPackageDto>? = null
)

