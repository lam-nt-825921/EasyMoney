package com.example.easymoney.data.remote

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.Banner
import com.example.easymoney.domain.model.EKycStatus
import com.example.easymoney.domain.model.LoanProduct
import com.example.easymoney.domain.model.SupportLink
import javax.inject.Inject

/** Workflow #45 — REMOTE data source cho Home (banners, hot loans, eKYC status). */
class HomeRemoteDataSource @Inject constructor(
    private val apiService: HomeApiService
) {
    suspend fun getBanners(): Resource<List<Banner>> =
        safeApiCall("Get banners failed") { apiService.getBanners() }

    suspend fun getHotLoans(): Resource<List<LoanProduct>> =
        safeApiCall("Get hot loans failed") { apiService.getHotLoans() }

    suspend fun getEKycStatus(): Resource<EKycStatus> =
        safeApiCall("Get eKYC status failed") { apiService.getEKycStatus() }

    suspend fun getCustomerSupportLink(): Resource<SupportLink> =
        safeApiCall("Get customer support link failed") { apiService.getCustomerSupportLink() }
}
