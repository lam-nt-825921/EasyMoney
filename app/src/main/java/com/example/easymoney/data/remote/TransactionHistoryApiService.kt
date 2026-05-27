package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ApiResponse
import com.example.easymoney.domain.model.TransactionGroup
import retrofit2.http.GET

/** Workflow #45 — Transaction history endpoint. */
interface TransactionHistoryApiService {

    @GET("api/v1/transactions")
    suspend fun getTransactionHistory(): ApiResponse<List<TransactionGroup>>
}
