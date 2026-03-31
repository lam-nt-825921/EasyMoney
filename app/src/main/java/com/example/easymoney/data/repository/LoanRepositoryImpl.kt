package com.example.easymoney.data.repository

import com.example.easymoney.data.remote.LoanRemoteDataSource
import com.example.easymoney.data.remote.dto.toDomain
import com.example.easymoney.data.remote.mock.LoanMockDataSource
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanPackageModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoanRepositoryImpl(
    private val remoteDataSource: LoanRemoteDataSource,
    private val mockDataSource: LoanMockDataSource
) : LoanRepository {

    override fun getLoanPackages(customerId: String?): Flow<Resource<List<LoanPackageModel>>> = flow {
        emit(Resource.Loading)

        try {
            val remoteData = remoteDataSource.fetchLoanPackages(customerId).map { it.toDomain() }
            emit(Resource.Success(data = remoteData, isFromMock = false))
        } catch (throwable: Throwable) {
            val mockData = mockDataSource.getLoanPackages()
            if (mockData.isNotEmpty()) {
                emit(Resource.Success(data = mockData, isFromMock = true))
            } else {
                emit(Resource.Error(message = "Unable to fetch loan packages", throwable = throwable))
            }
        }
    }

    override suspend fun getLoanPackageById(id: String): Resource<LoanPackageModel> {
        return try {
            val remoteData = remoteDataSource.fetchLoanPackageById(id).toDomain()
            Resource.Success(data = remoteData, isFromMock = false)
        } catch (throwable: Throwable) {
            val mockData = mockDataSource.getLoanPackageById(id)
            if (mockData != null) {
                Resource.Success(data = mockData, isFromMock = true)
            } else {
                Resource.Error(message = "Loan package not found", throwable = throwable)
            }
        }
    }
}

