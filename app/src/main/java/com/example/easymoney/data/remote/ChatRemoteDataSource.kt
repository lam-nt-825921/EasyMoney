package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ChatMessageRequestDto
import com.example.easymoney.data.remote.dto.toDomain
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.ui.chatbot.ChatMessage
import javax.inject.Inject

/** Workflow #48 — REMOTE data source cho chatbot; map response DTO sang ChatMessage. */
class ChatRemoteDataSource @Inject constructor(
    private val apiService: ChatApiService
) {
    suspend fun sendMessage(text: String): Resource<ChatMessage> = try {
        val response = apiService.sendMessage(ChatMessageRequestDto(text))
        if (response.status == "success") {
            Resource.Success(response.data.toDomain())
        } else {
            Resource.Error(response.message ?: "Send message failed")
        }
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Network error")
    }
}
