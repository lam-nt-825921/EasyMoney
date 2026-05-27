package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.Banner
import com.example.easymoney.domain.model.LoanProduct
import com.example.easymoney.domain.model.EKycStatus
import com.example.easymoney.domain.model.SupportLink

interface HomeRepository {
    suspend fun getBanners(): Resource<List<Banner>>
    suspend fun getHotLoans(): Resource<List<LoanProduct>>
    suspend fun getRecommendedLoan(): Resource<LoanProduct>
    suspend fun getEKycStatus(): Resource<EKycStatus>
    suspend fun getCustomerSupportLink(): Resource<SupportLink>
}
