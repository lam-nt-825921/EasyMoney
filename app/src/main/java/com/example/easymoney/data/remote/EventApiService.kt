package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ApiResponse
import com.example.easymoney.domain.model.Event
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Workflow #45 — Event endpoints. */
interface EventApiService {

    @GET("api/v1/events/{id}")
    suspend fun getEventDetail(@Path("id") id: String): ApiResponse<Event>

    @POST("api/v1/events/{id}/join")
    suspend fun joinEvent(@Path("id") id: String): ApiResponse<Unit>
}
