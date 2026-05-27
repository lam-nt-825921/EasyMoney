package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.RedeemRewardResult
import com.example.easymoney.domain.model.RewardCatalogItem
import com.example.easymoney.domain.model.UserRewards

interface RewardRepository {
    suspend fun getRewardsCatalog(): Resource<UserRewards>
    suspend fun redeemReward(itemId: String): Resource<RedeemRewardResult>
    /** Workflow #7 — danh sách quà có thể đổi (catalog hiển thị). */
    suspend fun getRewardCatalogItems(): Resource<List<RewardCatalogItem>>
}
