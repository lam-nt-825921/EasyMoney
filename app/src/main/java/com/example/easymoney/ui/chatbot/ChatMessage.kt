package com.example.easymoney.ui.chatbot

enum class ChatRole { USER, BOT }

sealed interface ChatMessage {
    val id: String
    val role: ChatRole

    data class Text(
        override val id: String,
        override val role: ChatRole,
        val content: String
    ) : ChatMessage

    data class Action(
        override val id: String,
        override val role: ChatRole,
        val label: String,
        val target: ChatActionTarget
    ) : ChatMessage

    data class Card(
        override val id: String,
        override val role: ChatRole,
        val title: String,
        val body: String,
        val actions: List<ChatActionButton>
    ) : ChatMessage
}

data class ChatActionButton(
    val label: String,
    val target: ChatActionTarget
)

sealed interface ChatActionTarget {
    data class NavigateRoute(val route: String) : ChatActionTarget
    data class DialPhone(val phone: String) : ChatActionTarget
}
