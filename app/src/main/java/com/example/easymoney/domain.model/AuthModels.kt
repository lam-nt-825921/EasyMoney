package com.example.easymoney.domain.model

data class LoginRequest(
    val phone: String,
    val password: String
)

data class RegisterRequest(
    val phone: String,
    val fullName: String,
    val password: String
)

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int
)

data class RememberedAccount(
    val phone: String,
    val fullName: String,
    val lastLoginTimestamp: Long = System.currentTimeMillis()
)
