package com.example.easymoney.domain.model

data class Banner(
    val id: String,
    val imageUrl: String,
    val title: String,
    val targetType: String, // EVENT, LOAN, WEB
    val targetId: String? = null
)
