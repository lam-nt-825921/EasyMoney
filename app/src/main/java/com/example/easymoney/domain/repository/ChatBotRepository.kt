package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.ui.chatbot.ChatMessage

interface ChatBotRepository {
    /** Gửi câu hỏi của user, nhận phản hồi (text/card/action). */
    suspend fun sendMessage(text: String): Resource<ChatMessage>
}
