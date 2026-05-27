package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ApiResponse
import com.example.easymoney.domain.model.Banner
import com.example.easymoney.domain.model.EKycStatus
import com.example.easymoney.domain.model.LoanProduct
import com.example.easymoney.domain.model.SupportLink
import retrofit2.http.GET

/**
 * Workflow #45 — Home endpoints. Domain model dùng trực tiếp làm response type
 * (cùng style với [LoanApiService], Gson LOWER_CASE_WITH_UNDERSCORES).
 */
interface HomeApiService {

    @GET("api/v1/home/banners")
    suspend fun getBanners(): ApiResponse<List<Banner>>

    @GET("api/v1/home/hot-loans")
    suspend fun getHotLoans(): ApiResponse<List<LoanProduct>>

    @GET("api/v1/home/recommended-loan")
    suspend fun getRecommendedLoan(): ApiResponse<LoanProduct>

    @GET("api/v1/ekyc/status")
    suspend fun getEKycStatus(): ApiResponse<EKycStatus>

    @GET("api/v1/support/cskh")
    suspend fun getCustomerSupportLink(): ApiResponse<SupportLink>
}
