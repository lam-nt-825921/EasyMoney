package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ApiResponse
import com.example.easymoney.data.remote.dto.ChatMessageRequestDto
import com.example.easymoney.data.remote.dto.ChatResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

/** Workflow #48 — Chatbot endpoint. Response hỗ trợ text/card/action. */
interface ChatApiService {

    @POST("api/v1/chat/message")
    suspend fun sendMessage(@Body request: ChatMessageRequestDto): ApiResponse<ChatResponseDto>
}
