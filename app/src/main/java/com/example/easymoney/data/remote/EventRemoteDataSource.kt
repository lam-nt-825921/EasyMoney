package com.example.easymoney.data.remote

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.Event
import javax.inject.Inject

/** Workflow #45 — REMOTE data source cho Event detail + join. */
class EventRemoteDataSource @Inject constructor(
    private val apiService: EventApiService
) {
    suspend fun getEventDetail(id: String): Resource<Event> =
        safeApiCall("Get event detail failed") { apiService.getEventDetail(id) }

    suspend fun joinEvent(id: String): Resource<Unit> =
        safeApiCall("Join event failed") { apiService.joinEvent(id) }
}
