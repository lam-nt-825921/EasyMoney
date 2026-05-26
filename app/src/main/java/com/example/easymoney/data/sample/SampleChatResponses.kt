package com.example.easymoney.data.sample

import com.example.easymoney.navigation.AppDestination
import com.example.easymoney.ui.chatbot.ChatActionButton
import com.example.easymoney.ui.chatbot.ChatActionTarget
import com.example.easymoney.ui.chatbot.ChatMessage
import com.example.easymoney.ui.chatbot.ChatRole
import java.util.UUID

/** Workflow #32 — Rule-based mock chatbot responses (di chuyển từ ChatBotViewModel). */
fun sampleChatReply(userText: String): ChatMessage {
    val lower = userText.lowercase()
    val id = UUID.randomUUID().toString()
    return when {
        "vay" in lower || "khoản vay" in lower -> ChatMessage.Card(
            id = id,
            role = ChatRole.BOT,
            title = "Gói vay phù hợp",
            body = "Bạn có thể xem các gói vay ưu đãi đang được áp dụng.",
            actions = listOf(
                ChatActionButton("Xem gói vay", ChatActionTarget.NavigateRoute(AppDestination.LoanList.route))
            )
        )
        "đổi điểm" in lower || "thưởng" in lower -> ChatMessage.Card(
            id = id,
            role = ChatRole.BOT,
            title = "Đổi điểm thưởng",
            body = "Mở danh sách quà có thể đổi.",
            actions = listOf(
                ChatActionButton("Mở đổi điểm", ChatActionTarget.NavigateRoute(AppDestination.Rewards.route))
            )
        )
        "hotline" in lower || "tổng đài" in lower -> ChatMessage.Action(
            id = id,
            role = ChatRole.BOT,
            label = "Gọi tổng đài 19001234",
            target = ChatActionTarget.DialPhone("19001234")
        )
        "hợp đồng" in lower || "quản lý" in lower -> ChatMessage.Card(
            id = id,
            role = ChatRole.BOT,
            title = "Quản lý khoản vay",
            body = "Xem các hợp đồng đã duyệt.",
            actions = listOf(
                ChatActionButton("Mở quản lý", ChatActionTarget.NavigateRoute(AppDestination.LoanManagement.route))
            )
        )
        else -> ChatMessage.Text(
            id,
            ChatRole.BOT,
            "Tôi chưa rõ câu hỏi. Thử hỏi về \"khoản vay\", \"đổi điểm\", \"hotline\" hoặc \"hợp đồng\"."
        )
    }
}
