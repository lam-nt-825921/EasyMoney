package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.LoanPackageDto
import com.example.easymoney.data.remote.dto.LoanPackagesResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface LoanApiService {
    @GET("loan-packages")
    suspend fun getLoanPackages(
        @Query("customerId") customerId: String? = null
    ): LoanPackagesResponseDto

    @GET("loan-packages/{id}")
    suspend fun getLoanPackageById(
        @Path("id") id: String
    ): LoanPackageDto
}

