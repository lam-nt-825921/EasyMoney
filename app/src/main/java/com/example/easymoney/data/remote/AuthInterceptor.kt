package com.example.easymoney.data.remote

import com.example.easymoney.data.local.AppPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val appPreferences: AppPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val accessToken = appPreferences.accessToken

        if (accessToken.isNullOrBlank() || path in publicAuthPaths) {
            return chain.proceed(request)
        }

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        return chain.proceed(authenticatedRequest)
    }

    private companion object {
        val publicAuthPaths = setOf(
            "/api/v1/auth/login",
            "/api/v1/auth/register"
        )
    }
}
