package com.example.easymoney.domain.repository

import com.example.easymoney.domain.common.Resource
import com.example.easymoney.domain.model.UserRewards

interface RewardRepository {
    suspend fun getRewardsCatalog(): Resource<UserRewards>
    suspend fun redeemReward(itemId: String): Resource<Unit>
}
