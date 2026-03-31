package com.example.easymoney.domain.common

sealed class Resource<out T> {
    data object Loading : Resource<Nothing>()

    data class Success<T>(
        val data: T,
        val isFromMock: Boolean = false
    ) : Resource<T>()

    data class Error<T>(
        val message: String,
        val throwable: Throwable? = null,
        val data: T? = null
    ) : Resource<T>()
}

