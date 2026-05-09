package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.Banner
import com.example.easymoney.domain.model.LoanProduct
import com.example.easymoney.domain.model.EKycStatus

interface HomeRepository {
    suspend fun getBanners(): Resource<List<Banner>>
    suspend fun getHotLoans(): Resource<List<LoanProduct>>
    suspend fun getEKycStatus(): Resource<EKycStatus>
}
