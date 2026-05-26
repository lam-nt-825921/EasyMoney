package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ChatResponseDto
import com.example.easymoney.data.remote.dto.toDomain
import com.example.easymoney.ui.chatbot.ChatActionTarget
import com.example.easymoney.ui.chatbot.ChatMessage
import com.example.easymoney.ui.chatbot.ChatRole
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Workflow #52 — mapping test cho ChatResponseDto (workflow #48). */
class ChatResponseDtoMappingTest {

    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()

    @Test
    fun `text type maps to ChatMessage Text with BOT role`() {
        val dto = gson.fromJson(
            """{ "id": "m1", "type": "text", "content": "Xin chào" }""",
            ChatResponseDto::class.java
        )
        val msg = dto.toDomain()
        assertTrue(msg is ChatMessage.Text)
        assertEquals(ChatRole.BOT, msg.role)
        assertEquals("Xin chào", (msg as ChatMessage.Text).content)
    }

    @Test
    fun `action type with dial maps to DialPhone target`() {
        val dto = gson.fromJson(
            """{ "type": "action", "label": "Gọi tổng đài", "action": { "kind": "dial", "phone": "19001234" } }""",
            ChatResponseDto::class.java
        )
        val msg = dto.toDomain()
        assertTrue(msg is ChatMessage.Action)
        val action = msg as ChatMessage.Action
        assertEquals("Gọi tổng đài", action.label)
        assertTrue(action.target is ChatActionTarget.DialPhone)
        assertEquals("19001234", (action.target as ChatActionTarget.DialPhone).phone)
    }

    @Test
    fun `card type maps buttons with navigate targets`() {
        val dto = gson.fromJson(
            """
            {
                "type": "card",
                "title": "Gói vay",
                "body": "Xem chi tiết",
                "actions": [ { "label": "Mở", "action": { "kind": "navigate", "route": "loan_list" } } ]
            }
            """.trimIndent(),
            ChatResponseDto::class.java
        )
        val msg = dto.toDomain()
        assertTrue(msg is ChatMessage.Card)
        val card = msg as ChatMessage.Card
        assertEquals(1, card.actions.size)
        assertTrue(card.actions[0].target is ChatActionTarget.NavigateRoute)
        assertEquals("loan_list", (card.actions[0].target as ChatActionTarget.NavigateRoute).route)
    }

    @Test
    fun `unknown type falls back to Text`() {
        val dto = gson.fromJson("""{ "type": "weird", "content": "x" }""", ChatResponseDto::class.java)
        assertTrue(dto.toDomain() is ChatMessage.Text)
    }
}
