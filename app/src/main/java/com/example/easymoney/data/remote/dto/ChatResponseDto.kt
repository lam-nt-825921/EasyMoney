package com.example.easymoney.data.remote.dto

import com.example.easymoney.ui.chatbot.ChatActionButton
import com.example.easymoney.ui.chatbot.ChatActionTarget
import com.example.easymoney.ui.chatbot.ChatMessage
import com.example.easymoney.ui.chatbot.ChatRole
import java.util.UUID

/**
 * Workflow #48 — Chatbot response transport. `type` phân biệt component:
 * `text` | `action` | `card`. Action target `kind`: `navigate` | `dial`.
 */
data class ChatMessageRequestDto(
    val text: String
)

data class ChatResponseDto(
    val id: String? = null,
    val type: String? = null,
    val content: String? = null,
    val label: String? = null,
    val title: String? = null,
    val body: String? = null,
    val action: ChatActionTargetDto? = null,
    val actions: List<ChatActionButtonDto>? = null
)

data class ChatActionButtonDto(
    val label: String? = null,
    val action: ChatActionTargetDto? = null
)

data class ChatActionTargetDto(
    val kind: String? = null,
    val route: String? = null,
    val phone: String? = null
)

private fun ChatActionTargetDto?.toDomain(): ChatActionTarget = when (this?.kind) {
    "dial" -> ChatActionTarget.DialPhone(phone.orEmpty())
    else -> ChatActionTarget.NavigateRoute(this?.route.orEmpty())
}

/** Map response DTO sang [ChatMessage] (luôn role BOT). Fallback về Text nếu type lạ. */
fun ChatResponseDto.toDomain(): ChatMessage {
    val messageId = id ?: UUID.randomUUID().toString()
    return when (type) {
        "action" -> ChatMessage.Action(
            id = messageId,
            role = ChatRole.BOT,
            label = label.orEmpty(),
            target = action.toDomain()
        )
        "card" -> ChatMessage.Card(
            id = messageId,
            role = ChatRole.BOT,
            title = title.orEmpty(),
            body = body.orEmpty(),
            actions = actions.orEmpty().map {
                ChatActionButton(label = it.label.orEmpty(), target = it.action.toDomain())
            }
        )
        else -> ChatMessage.Text(
            id = messageId,
            role = ChatRole.BOT,
            content = content.orEmpty()
        )
    }
}
