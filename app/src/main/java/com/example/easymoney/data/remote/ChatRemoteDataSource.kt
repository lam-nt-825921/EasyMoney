package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ChatMessageRequestDto
import com.example.easymoney.data.remote.dto.toDomain
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.ui.chatbot.ChatMessage
import javax.inject.Inject

/** Workflow #48/#59 — REMOTE data source cho chatbot; map response DTO sang ChatMessage. */
class ChatRemoteDataSource @Inject constructor(
    private val apiService: ChatApiService
) {
    suspend fun sendMessage(text: String): Resource<ChatMessage> =
        safeApiCall("Send message failed") { apiService.sendMessage(ChatMessageRequestDto(text)) }
            .mapSuccess { it.toDomain() }
}
