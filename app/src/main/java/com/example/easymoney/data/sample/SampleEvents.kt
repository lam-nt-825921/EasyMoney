package com.example.easymoney.data.sample

import com.example.easymoney.domain.model.Event
import com.example.easymoney.domain.model.EventInteractionType

fun sampleEvent(id: String): Event = Event(
    id = id,
    title = "Ưu đãi lãi suất 0% cho người mới",
    content = "Chào mừng bạn gia nhập EasyMoney. Nhận ngay ưu đãi 0% lãi suất cho khoản vay đầu tiên dưới 5 triệu đồng trong 15 ngày.",
    imageUrl = "https://img.freepik.com/free-vector/horizontal-banner-template-with-finance-concept_23-2149156382.jpg",
    expiryDate = System.currentTimeMillis() + 86_400_000L * 30,
    interactionType = EventInteractionType.NATIVE
)
