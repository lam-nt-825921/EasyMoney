package com.example.easymoney.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.repository.ChatBotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ChatBotUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isThinking: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ChatBotViewModel @Inject constructor(
    private val chatBotRepository: ChatBotRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatBotUiState())
    val uiState: StateFlow<ChatBotUiState> = _uiState.asStateFlow()

    init {
        appendBot(ChatMessage.Text(newId(), ChatRole.BOT, "Xin chào! Tôi có thể giúp gì cho bạn?"))
    }

    fun onInputChange(value: String) {
        _uiState.update { it.copy(input = value) }
    }

    fun onSend() {
        val text = _uiState.value.input.trim()
        if (text.isEmpty()) return
        appendUser(text)
        _uiState.update { it.copy(input = "", isThinking = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = chatBotRepository.sendMessage(text)) {
                is Resource.Success -> {
                    appendBot(result.data)
                    _uiState.update { it.copy(isThinking = false) }
                }
                is Resource.Error -> _uiState.update {
                    it.copy(isThinking = false, errorMessage = result.message)
                }
                Resource.Loading -> Unit
            }
        }
    }

    private fun appendUser(text: String) {
        val msg = ChatMessage.Text(newId(), ChatRole.USER, text)
        _uiState.update { it.copy(messages = it.messages + msg) }
    }

    private fun appendBot(message: ChatMessage) {
        _uiState.update { it.copy(messages = it.messages + message) }
    }

    private fun newId(): String = UUID.randomUUID().toString()
}
