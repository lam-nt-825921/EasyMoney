package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.LoanPackageModel

interface LoanRepository {
    suspend fun getLoanPackageById(id: String): Resource<LoanPackageModel>
}