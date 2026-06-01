package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ApiResponse
import com.example.easymoney.data.remote.dto.TransactionGroupDto
import retrofit2.http.GET

/** Workflow #45/#60 — Transaction history endpoint, dùng DTO riêng để giữ timestamp. */
interface TransactionHistoryApiService {

    @GET("api/v1/transactions")
    suspend fun getTransactionHistory(): ApiResponse<List<TransactionGroupDto>>
}
