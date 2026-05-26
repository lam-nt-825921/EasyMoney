package com.example.easymoney.data.remote

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.RewardCatalogItem
import com.example.easymoney.domain.model.UserRewards
import javax.inject.Inject

/** Workflow #45 — REMOTE data source cho Reward catalog/user/redeem. */
class RewardRemoteDataSource @Inject constructor(
    private val apiService: RewardApiService
) {
    suspend fun getRewardCatalogItems(): Resource<List<RewardCatalogItem>> =
        safeApiCall("Get reward catalog failed") { apiService.getRewardCatalogItems() }

    suspend fun getRewardsCatalog(): Resource<UserRewards> =
        safeApiCall("Get user rewards failed") { apiService.getRewardsCatalog() }

    suspend fun redeemReward(itemId: String): Resource<Unit> =
        safeApiCall("Redeem reward failed") { apiService.redeemReward(itemId) }
}
