package com.example.easymoney.domain.model

data class MyInfoModel(
    val fullName: String,
    val gender: String,
    val dateOfBirth: String,
    val phoneNumber: String,
    val nationalId: String,
    val issueDate: String,
    // Add address info
    val permanentProvince: String? = null,
    val permanentDistrict: String? = null,
    val permanentWard: String? = null,
    val permanentDetail: String? = null
)
