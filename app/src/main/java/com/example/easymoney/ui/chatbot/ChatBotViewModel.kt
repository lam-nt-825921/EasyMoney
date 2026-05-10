package com.example.easymoney.ui.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.easymoney.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
    val isThinking: Boolean = false
)

@HiltViewModel
class ChatBotViewModel @Inject constructor() : ViewModel() {

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
        _uiState.update { it.copy(input = "") }
        respond(text)
    }

    private fun respond(userText: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isThinking = true) }
            delay(600)
            val lower = userText.lowercase()
            val reply = when {
                "vay" in lower || "khoản vay" in lower -> ChatMessage.Card(
                    id = newId(),
                    role = ChatRole.BOT,
                    title = "Gói vay phù hợp",
                    body = "Bạn có thể xem các gói vay ưu đãi đang được áp dụng.",
                    actions = listOf(
                        ChatActionButton("Xem gói vay", ChatActionTarget.NavigateRoute(AppDestination.LoanList.route))
                    )
                )
                "đổi điểm" in lower || "thưởng" in lower -> ChatMessage.Card(
                    id = newId(),
                    role = ChatRole.BOT,
                    title = "Đổi điểm thưởng",
                    body = "Mở danh sách quà có thể đổi.",
                    actions = listOf(
                        ChatActionButton("Mở đổi điểm", ChatActionTarget.NavigateRoute(AppDestination.Rewards.route))
                    )
                )
                "hotline" in lower || "tổng đài" in lower -> ChatMessage.Action(
                    id = newId(),
                    role = ChatRole.BOT,
                    label = "Gọi tổng đài 19001234",
                    target = ChatActionTarget.DialPhone("19001234")
                )
                "hợp đồng" in lower || "quản lý" in lower -> ChatMessage.Card(
                    id = newId(),
                    role = ChatRole.BOT,
                    title = "Quản lý khoản vay",
                    body = "Xem các hợp đồng đã duyệt.",
                    actions = listOf(
                        ChatActionButton("Mở quản lý", ChatActionTarget.NavigateRoute(AppDestination.LoanManagement.route))
                    )
                )
                else -> ChatMessage.Text(
                    newId(), ChatRole.BOT,
                    "Tôi chưa rõ câu hỏi. Thử hỏi về \"khoản vay\", \"đổi điểm\", \"hotline\" hoặc \"hợp đồng\"."
                )
            }
            appendBot(reply)
            _uiState.update { it.copy(isThinking = false) }
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
