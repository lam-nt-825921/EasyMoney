package com.example.easymoney.domain.repository

import android.util.Log
import com.example.easymoney.data.local.AppPreferences
import com.example.easymoney.data.local.DataSourceMode
import com.example.easymoney.data.remote.ChatRemoteDataSource
import com.example.easymoney.data.sample.sampleChatReply
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.ui.chatbot.ChatMessage
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TAG = "DataSource"

class ChatBotRepositoryImpl @Inject constructor(
    private val appPreferences: AppPreferences,
    private val remoteDataSource: ChatRemoteDataSource
) : ChatBotRepository {

    override suspend fun sendMessage(text: String): Resource<ChatMessage> {
        val mode = appPreferences.dataSourceMode
        Log.d(TAG, "ChatBotRepository.sendMessage mode=$mode")
        return when (mode) {
            DataSourceMode.MOCK -> {
                delay(600)
                Resource.Success(sampleChatReply(text), isFromMock = true)
            }
            DataSourceMode.REMOTE -> remoteDataSource.sendMessage(text)
        }
    }
}
