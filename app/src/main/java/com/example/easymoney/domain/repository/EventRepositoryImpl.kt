package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.Event
import com.example.easymoney.domain.model.EventInteractionType
import kotlinx.coroutines.delay
import javax.inject.Inject

class EventRepositoryImpl @Inject constructor() : EventRepository {
    override suspend fun getEventDetail(id: String): Resource<Event> {
        delay(500)
        return Resource.Success(
            Event(
                id = id,
                title = "Ưu đãi lãi suất 0% cho người mới",
                content = "Chào mừng bạn gia nhập EasyMoney. Nhận ngay ưu đãi 0% lãi suất cho khoản vay đầu tiên dưới 5 triệu đồng trong 15 ngày.",
                imageUrl = "https://img.freepik.com/free-vector/horizontal-banner-template-with-finance-concept_23-2149156382.jpg",
                expiryDate = System.currentTimeMillis() + 86400000 * 30,
                interactionType = EventInteractionType.NATIVE
            )
        )
    }

    override suspend fun joinEvent(id: String): Resource<Unit> {
        delay(300)
        return Resource.Success(Unit)
    }
}
