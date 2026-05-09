package com.example.easymoney.domain.model

enum class EventInteractionType {
    NATIVE, WEBVIEW
}

data class Event(
    val id: String,
    val title: String,
    val content: String,
    val imageUrl: String,
    val expiryDate: Long?,
    val interactionType: EventInteractionType,
    val actionUrl: String? = null
)
