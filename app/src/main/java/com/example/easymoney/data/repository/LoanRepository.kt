package com.example.easymoney.data.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanPackageModel
import kotlinx.coroutines.flow.Flow

interface LoanRepository {
    fun getLoanPackages(customerId: String? = null): Flow<Resource<List<LoanPackageModel>>>
    suspend fun getLoanPackageById(id: String): Resource<LoanPackageModel>
}

