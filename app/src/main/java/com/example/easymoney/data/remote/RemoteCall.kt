package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ApiResponse
import com.example.easymoney.domain.common.Resource

/**
 * Workflow #45/#59 — Bọc một Retrofit call: bắt lỗi mạng và kiểm tra `status`/`data`
 * của [ApiResponse]. `data` nullable theo workflow #59; nếu success nhưng data == null
 * thì coi là Error để callers không phải tự lo nullability.
 */
suspend fun <T> safeApiCall(
    failMessage: String,
    call: suspend () -> ApiResponse<T>
): Resource<T> = try {
    val response = call()
    when {
        response.status != "success" ->
            Resource.Error(userFriendlyErrorMessage(response.message, failMessage))
        response.data == null ->
            Resource.Error(userFriendlyErrorMessage(response.message, failMessage))
        else -> Resource.Success(response.data)
    }
} catch (e: Exception) {
    Resource.Error(userFriendlyErrorMessage(e, failMessage))
}

/**
 * Workflow #59 — dùng cho endpoints chỉ quan tâm tới `status` (không có payload có ích).
 * Ví dụ `POST /otp/send`, `DELETE .../clear`, `POST .../sign` — backend có thể trả
 * `null`, `{}`, hay `ApiResponse[None]`; helper này không đụng vào `data`.
 */
suspend fun safeUnitApiCall(
    failMessage: String,
    call: suspend () -> ApiResponse<*>
): Resource<Unit> = try {
    val response = call()
    if (response.status == "success") Resource.Success(Unit)
    else Resource.Error(userFriendlyErrorMessage(response.message, failMessage))
} catch (e: Exception) {
    Resource.Error(userFriendlyErrorMessage(e, failMessage))
}

/**
 * Workflow #59 — transform `Resource.Success` payload while preserving Error/Loading.
 * Tiện cho callers cần map DTO sang domain sau khi đã có Resource.
 */
inline fun <T, R> Resource<T>.mapSuccess(transform: (T) -> R): Resource<R> = when (this) {
    is Resource.Success -> Resource.Success(transform(data), isFromMock = isFromMock)
    is Resource.Error -> Resource.Error(message, throwable)
    is Resource.Loading -> Resource.Loading
}
