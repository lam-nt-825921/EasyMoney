package com.example.easymoney.data.remote

import com.example.easymoney.data.remote.dto.ApiResponse
import com.example.easymoney.domain.model.RewardCatalogItem
import com.example.easymoney.domain.model.UserRewards
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Workflow #45 — Reward endpoints. */
interface RewardApiService {

    @GET("api/v1/rewards/catalog")
    suspend fun getRewardCatalogItems(): ApiResponse<List<RewardCatalogItem>>

    @GET("api/v1/rewards/user")
    suspend fun getRewardsCatalog(): ApiResponse<UserRewards>

    @POST("api/v1/rewards/{itemId}/redeem")
    suspend fun redeemReward(@Path("itemId") itemId: String): ApiResponse<Unit>
}
