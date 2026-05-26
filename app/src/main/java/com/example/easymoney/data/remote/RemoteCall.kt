package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ApiResponse
import com.example.easymoney.domain.common.Resource

/**
 * Workflow #45 — Bọc một lời gọi Retrofit: bắt lỗi mạng và kiểm tra `status`
 * của [ApiResponse]. Dùng chung cho mọi RemoteDataSource.
 */
suspend fun <T> safeApiCall(
    failMessage: String,
    call: suspend () -> ApiResponse<T>
): Resource<T> = try {
    val response = call()
    if (response.status == "success") {
        Resource.Success(response.data)
    } else {
        Resource.Error(response.message ?: failMessage)
    }
} catch (e: Exception) {
    Resource.Error(e.message ?: "Network error")
}
