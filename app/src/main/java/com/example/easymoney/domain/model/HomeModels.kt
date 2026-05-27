package com.example.easymoney.domain.model

data class Banner(
    val id: String,
    val imageUrl: String,
    val title: String,
    val targetType: String, // EVENT, LOAN, WEB
    val targetId: String? = null
)

data class SupportLink(
    val title: String,
    val path: String,
    val url: String,
    val phone: String,
    val email: String,
    val availableTime: String
)
