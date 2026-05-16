package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.sample.sampleChatReply
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.ui.chatbot.ChatMessage
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "DataSource"
private const val REMOTE_NOT_READY = "Endpoint REMOTE chatbot chưa sẵn sàng — vui lòng chuyển Sandbox sang MOCK"

class ChatBotRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences
) : ChatBotRepository {

    override suspend fun sendMessage(text: String): Resource<ChatMessage> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "ChatBotRepository.sendMessage mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(600)
                Resource.Success(sampleChatReply(text), isFromMock = true)
            }
            DataSourceMode.REMOTE -> {
                // TODO(workflow_32): wire real POST /chat/message endpoint
                Resource.Error(REMOTE_NOT_READY)
            }
        }
    }
}
