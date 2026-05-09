package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.Event

interface EventRepository {
    suspend fun getEventDetail(id: String): Resource<Event>
    suspend fun joinEvent(id: String): Resource<Unit>
}
