package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.LoanPackageDto

interface LoanRemoteDataSource {
    suspend fun fetchLoanPackages(customerId: String? = null): List<LoanPackageDto>
    suspend fun fetchLoanPackageById(id: String): LoanPackageDto
}

class LoanRemoteDataSourceImpl(
    private val apiService: LoanApiService
) : LoanRemoteDataSource {
    override suspend fun fetchLoanPackages(customerId: String?): List<LoanPackageDto> {
        return apiService.getLoanPackages(customerId).items.orEmpty()
    }

    override suspend fun fetchLoanPackageById(id: String): LoanPackageDto {
        return apiService.getLoanPackageById(id)
    }
}

