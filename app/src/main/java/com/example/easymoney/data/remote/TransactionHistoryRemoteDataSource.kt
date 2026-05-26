package com.example.easymoney.data.remote

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.TransactionGroup
import javax.inject.Inject

/** Workflow #45 — REMOTE data source cho lịch sử giao dịch. */
class TransactionHistoryRemoteDataSource @Inject constructor(
    private val apiService: TransactionHistoryApiService
) {
    suspend fun getTransactionHistory(): Resource<List<TransactionGroup>> =
        safeApiCall("Get transaction history failed") { apiService.getTransactionHistory() }
}
